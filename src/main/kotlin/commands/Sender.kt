package commands

import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonNull
import space.kscience.magix.api.MagixEndpoint
import space.kscience.magix.api.MagixMessage
import space.kscience.magix.api.send
import space.kscience.magix.rsocket.rSocketWithTcp

/// Sends single test message to Magix
// TODO закрыть канал (и корутину)
suspend fun main(): Unit = coroutineScope {
    val sendEndpoint = MagixEndpoint.rSocketWithTcp(
        "localhost", 7778
    )
    sendEndpoint.send(MagixMessage(
        "controls-kt",
        JsonNull,
        sourceEndpoint = "controls-kt"
    ))
    sendEndpoint.close()
    println("message sent")
}