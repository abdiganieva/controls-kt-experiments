package commands

import devices.IMeradatVacDevice
import devices.ISinCosDevice
import io.ktor.client.engine.js.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import space.kscience.controls.client.controlsPropertyFlow
import space.kscience.dataforge.names.Name
import space.kscience.magix.api.MagixEndpoint
import space.kscience.magix.rsocket.rSocketWithWebSockets
import storage.StorageClient
import kotlin.time.Duration.Companion.seconds

// declare external plotting functions defined in embedded script in index.html page
external fun plot(y: Array<Double>)
external fun plotHistory(x: Array<String>, y: Array<Double>)

// Этот пример показывает подключение к property девайса без DeviceClient.
// This example fits the case when client need just a little number of device properties
// NOTE: для работы этого скрипта [Main.kt](../Main.kt) должен быть запущен
suspend fun main(): Unit = coroutineScope {

    // Получение истории property девайса за последнюю минуту
    run {
        val client = StorageClient(Js)
        val start = (Clock.System.now() - 60.seconds).toLocalDateTime(TimeZone.currentSystemDefault())
        //val out = client.read("meradat", IMeradatVacDevice.pressure, start=start)
        //plot(client.read("meradat", ))
        // отображение графика
        //TODO()
        //val x = out.map { it.first.toString() }.toTypedArray()
        //val y = out.map { it.second }.toTypedArray()
        //plotHistory(x, y)
    }

    // подключение к Magix
    val sendEndpoint = MagixEndpoint.rSocketWithWebSockets(
        "localhost", 7777
    )
    println("--before launch")
    /*launch {
        val container = mutableListOf<Double>()
        println("----b4 send endpoint")
        sendEndpoint.controlsPropertyFlow(
            "controls-kt", Name.of("meradat"), IMeradatVacDevice.pressure).collect {
                println("---- FLOW $it")
            container += it
            println("b4 plot")
            plot(container.toTypedArray())
            println("after plot")
        }
    } */
}
