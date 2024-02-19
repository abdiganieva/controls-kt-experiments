package commands

import devices.SinCosDevice
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import space.kscience.controls.api.Device
import space.kscience.controls.api.onPropertyChange
import space.kscience.controls.api.propertyMessageFlow
import space.kscience.controls.client.remoteDevice
import space.kscience.controls.spec.DevicePropertySpec
import space.kscience.controls.spec.name
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.names.Name
import space.kscience.magix.api.MagixEndpoint
import space.kscience.magix.rsocket.rSocketWithWebSockets

suspend fun <T> Device.readUnsafe(propertySpec: DevicePropertySpec<*, T>): T =
    propertySpec.converter.metaToObject(readProperty(propertySpec.name)) ?: error("Property read result is not valid")

/// Sends single test message to Magix
// Пример управления удаленным девайсом через Magix
// NOTE: для работы этого скрипта [Main.kt](../Main.kt) должен быть запущен
// NOTE: API девайса во многом не работает
suspend fun main(): Unit = coroutineScope {

    // подключение к Magix
    val sendEndpoint = MagixEndpoint.rSocketWithWebSockets(
        "localhost", 7777
    )

    // подключение к удаленному девайсу
    val device = run {
        val context = Context("clientContext") {}
        val device = sendEndpoint.remoteDevice(
            context,
            "controls-kt",
            Name.of("demo")
        )
        device
    }


    run {
        // time не доступен таким методом, т.к. он не прописан в спеке
        val sinScale = device.readUnsafe(SinCosDevice.sinScale)
        val sin = device.readUnsafe(SinCosDevice.sin)
        println("""Device attributes from spec:
        time=(unavailable from spec)
        sinScale=$sinScale
        sin=$sin
        """.trimIndent())

        // изменение аттрибута в устройства
        // NOTE: не работает (нет реализации writeUnsafe)
//        device.write(SinCosDevice.sinScale, 2.0)
    }

    // Подписка на все изменения
    device.onPropertyChange {
        println("catch general prop change: $this")
    }

    // Подписка на конкретное изменение
    device.propertyMessageFlow("sin").onEach {
        println("catch specific prop change (sin): $it")
    }.launchIn(this)
}