package commands

import devices.SinCosDevice
import kotlinx.coroutines.coroutineScope
import space.kscience.controls.client.controlsPropertyFlow
import space.kscience.dataforge.names.Name
import space.kscience.magix.api.MagixEndpoint
import space.kscience.magix.rsocket.rSocketWithWebSockets

// Этот пример показывает подключение к property девайса без DeviceClient.
// This example fits the case when client need just a little number of device properties
// NOTE: для работы этого скрипта [Main.kt](../Main.kt) должен быть запущен
suspend fun main(): Unit = coroutineScope {

    // подключение к Magix
    val sendEndpoint = MagixEndpoint.rSocketWithWebSockets(
        "localhost", 7777
    )

    sendEndpoint.controlsPropertyFlow(
        "controls-kt", Name.of("demo"), SinCosDevice.sin).collect {
            println("catch specific property change (sin): ${it}")
    }
}