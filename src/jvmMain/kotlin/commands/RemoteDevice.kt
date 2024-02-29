package commands

import devices.ISinCosDevice
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import space.kscience.controls.api.onPropertyChange
import space.kscience.controls.api.propertyMessageFlow
import space.kscience.controls.client.read
import space.kscience.controls.client.remoteDevice
import space.kscience.controls.client.write
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.names.Name
import space.kscience.magix.api.MagixEndpoint
import space.kscience.magix.rsocket.rSocketWithWebSockets

// Этот пример показывает управление через DeviceClient.
// This example fits the case when client need to use device properties heavily
// (need access to almost device properties).
// NOTE: для работы этого скрипта [Main.kt](../Main.kt) должен быть запущен
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
            "controls-kt",
            Name.of("demo")
        )
        device
    }


    run {
        // time не доступен таким методом, т.к. он не прописан в спеке
        val sinScale = device.read(ISinCosDevice.sinScale)
        val sin = device.read(ISinCosDevice.sin)
        println("""Device attributes from spec:
        time=(unavailable from spec)
        sinScale=$sinScale
        sin=$sin
        """.trimIndent())

        // изменение аттрибута в устройства
        device.write(ISinCosDevice.sinScale, 2.0)
    }

    // Подписка на все изменения
    device.onPropertyChange {
        println("catch general prop change: $this")
    }

//    val flow = sendEndpoint.controlsPropertyFlow("controls-kt", Name.of("demo"), SinCosDevice.sin)

    // Подписка на конкретное изменение
    device.propertyMessageFlow("sin").onEach {
        println("catch specific prop change (sin): $it")
    }.launchIn(this)
}