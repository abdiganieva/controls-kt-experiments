import devices.SinCosDevice
import kotlinx.coroutines.coroutineScope
import space.kscience.controls.client.launchMagixService
import space.kscience.controls.manager.DeviceManager
import space.kscience.controls.manager.install
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.request
import space.kscience.dataforge.meta.Meta
import space.kscience.magix.api.MagixEndpoint
import space.kscience.magix.rsocket.rSocketWithTcp
import space.kscience.magix.server.RSocketMagixFlowPlugin
import space.kscience.magix.server.startMagixServer

/// Create demo device and Magix server
//  and connect to each other inside one program
suspend fun main(): Unit = coroutineScope {

    /// Создание менеджера устройства
    val context = Context("clientContext") {
        plugin(DeviceManager)
    }
    // Создание девайс менеджера напрямую не рекомендовано
    // TODO: писать предупреждение, если девайс менеджер создается напрямую
    val manager = context.request(DeviceManager)

    val device = SinCosDevice.build(context, Meta.EMPTY)
    // register device and open it
    manager.install("demo", device)
    // just register device
    // TODO: зачем нужнен этот метод?
    // manager.registerDevice(NameToken("device"), device);

    // another method to add device
    // TODO: доделать
    // val device by manager.installing(SinCosDevice)

    // Start magix (?) server (web ui by default = http://localhost:7777)
    // Похоже, что поднимается только веб морда с возможностью синхронных запросов
    // wireshark не показывает ничего больше
    // TODO: почему используется дефолтный порт magix?
    // startDeviceServer(manager, port = 7778)

    /// Запуск реального magix сервера
    // TODO: что эта штука делает?
    // TODO: почему не работает вместе со startDeviceServer,
    //  даже если вручную выставить разные порты?
    startMagixServer(
        RSocketMagixFlowPlugin(serverPort = 7778), //TCP rsocket support
    )

    /// Connect device manager to Magix server via RSocket
    // TODO: сделать прямое подключение без TCP, когда оно будет имплементированно
    val magixEndpoint = MagixEndpoint.rSocketWithTcp(
        "localhost", port = 7778
    )
    manager.launchMagixService(magixEndpoint)
}