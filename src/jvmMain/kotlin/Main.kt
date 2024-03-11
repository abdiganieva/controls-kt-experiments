import devices.*
import devices.CM32Device.Companion.onOpen
import devices.MKSBaratronDevice.Companion.onOpen
import devices.MeradatVacDevice.Companion.onOpen
import devices.SinCosDevice.Companion.onOpen
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import space.kscience.controls.api.onPropertyChange
import space.kscience.controls.api.propertyMessageFlow
import space.kscience.controls.client.launchMagixService
import space.kscience.controls.manager.DeviceManager
import space.kscience.controls.manager.install
import space.kscience.controls.server.startDeviceServer
import space.kscience.controls.spec.read
import space.kscience.controls.spec.write
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.request
import space.kscience.dataforge.meta.Meta
import space.kscience.magix.api.MagixEndpoint
import space.kscience.magix.rsocket.rSocketWithWebSockets
import space.kscience.magix.server.RSocketMagixFlowPlugin
import space.kscience.magix.server.startMagixServer
import storage.startStorage
import kotlin.io.path.Path

/// Create demo device and Magix server
//  and connect to each other inside one program
// Эта программа:
// 1. Создает менеджер устройств (Нужен для взаимодействия девайсов с брокером Magix)
// 2. Создает тестовое устройство SinCosDevice и регистрирует его в менеджере
// 3. Запускает Magix endpoint, чтобы другие программы могли управлять тестовым устройством
suspend fun main(): Unit = coroutineScope {

    /// Создание менеджера устройства
    val context = Context("clientContext") {
        plugin(DeviceManager)
    }

    // Создание девайс менеджера напрямую не рекомендовано
    // TODO: писать предупреждение, если девайс менеджер создается напрямую
    val manager = context.request(DeviceManager)

    // создание тестового устройства
    // QUESTION: можно ли типизировать мету для устройства?
    val device = SinCosDevice.build(context, Meta.EMPTY)
    val CM32device = CM32Device.build(context, Meta.EMPTY)
    val meradat = MeradatVacDevice.build(context, Meta.EMPTY)
    val baratron = MKSBaratronDevice.build(context, Meta.EMPTY)

    // register device and open it
    //manager.install("demo", device)
    //manager.install("cm32", CM32device)
    manager.install("meradat", meradat)

    // just register device
    // TODO: зачем нужен этот метод?
    // manager.registerDevice(NameToken("device"), device);

    // another method to add device (на данный момент не работает)
    // [обсуждение](https://mm.sciprog.center/spc/pl/q5r4zz5rqjdidctfk4gguwjity)
    // TODO: доделать
    // val device by manager.installing(SinCosDevice)

    // Пока девайс с интерфейсом надо запускать вручную (баг)
    // TODO: исправить
    //device.onOpen()
    //CM32device.onOpen()
    meradat.onOpen()

    // Start magix (?) server (web ui = http://localhost:7776)
    // Поднимаем веб интерфейс менеджера устройств
    // NOTE: при одновременной работе c Magix сервером надо поменять порт,
    // чтобы он не конфликтовал с веб сокетами Magix
    // TODO: поменять дефолтный порт, чтобы он не конфликтовал с Magix server
    startDeviceServer(manager, port = 7776)

    // Подключение самодельного хранилища истории свойств девайса
    manager.startStorage(8080, Path("data/controls-kt"))

    /// Запуск Magix сервера
    // Без доп плагинов будет запущен websocket сервер на порте 7777.
    // В принципе этого должно быть достаточно.
    startMagixServer(
        RSocketMagixFlowPlugin(serverPort = 7778), // опциональный TCP плагин
        buffer = 1, // TODO: buffer = 0 not working
        port = 7777 // порт для веб сокетов (выставлен дефолтный)
    )

    // Примеры управления девайсом напрямую (не через Magix)
    run {
        println("""
        ${device.propertyDescriptors}
        ${device.actionDescriptors}
        """.trimIndent())
    }

    // 1. Управление девайсом по его интерфейсу
    // Я так и не понял, правильное это использование или нет
    // и зачем вообще нужен интерфейс устройства
    run {
        val time = device.time()
        val sinScale = device.sinScaleState
        // Другие параметры типа sin не доступны, т.к. они не прописаны в интерфейсе
        println("""Device attributes from interface:
        time=$time
        sinScale=$sinScale
        sin=(unavailable from interface)
        """.trimIndent())

        // изменение атрибута через интерфейс
        // NOTE: не уверен, что так правильно делать
        device.sinScaleState = 2.0
    }

    // 2. Управление девайсом через "спеку" девайса (см companion в [ISinCosDevice.kt](./devices/ISinCosDevice.kt)).
    // Это вроде как правильное обращение к девайсу
    run {
        // time не доступен таким методом, т.к. он не прописан в спеке
        val sinScale = device.read(ISinCosDevice.sinScale)
        val sin = device.read(ISinCosDevice.sin)
        println("""Device attributes from spec:
        time=(unavailable from spec)
        sinScale=$sinScale
        sin=$sin
        """.trimIndent())

        // изменение аттрибута устройства
        device.write(ISinCosDevice.sinScale, 2.0)
    }

    // Подписка на все изменения
    device.onPropertyChange {
        println("catch general prop change: $this")
    }

    // Подписка на конкретное изменение
    device.propertyMessageFlow("sin").onEach {
        println("catch specific prop change (sin): $it")
    }.launchIn(this)


    // Подключение менеджера к Magix
    // Необходимо для отправки изменений и реакции на сообщения из Magix
    // NOTE: прямое подключение менеджера (без сокетов) к Magix пока невозможно
    run {
        val magixEndpoint = MagixEndpoint.rSocketWithWebSockets(
            "localhost", port = 7777,
        )
        manager.launchMagixService(magixEndpoint)
    }

    // см пример управления девайсом через Magix в [RemoteDevice.kt](./commands/RemoteDevice.kt)
}