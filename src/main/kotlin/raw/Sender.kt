package raw

import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonNull
import space.kscience.magix.api.MagixEndpoint
import space.kscience.magix.api.MagixMessage
import space.kscience.magix.api.send
import space.kscience.magix.rsocket.rSocketWithWebSockets

/// Sends single test message to Magix
// NOTE: для работы этого скрипта [Main.kt](../Main.kt) должен быть запущен
// TODO закрыть канал (и корутину)
suspend fun main(): Unit = coroutineScope {
    val sendEndpoint = MagixEndpoint.rSocketWithWebSockets(
        "localhost", 7777
    )
    sendEndpoint.send(MagixMessage(
        "controls-kt",
        JsonNull,
        sourceEndpoint = "controls-kt",
        targetEndpoint = "controls-kt"
    ))
    sendEndpoint.close()
    println("message sent")
}