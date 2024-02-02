package commands

import kotlinx.coroutines.coroutineScope
import space.kscience.controls.api.PropertyGetMessage
import space.kscience.controls.api.toMeta
import space.kscience.dataforge.meta.toJson
import space.kscience.dataforge.names.Name
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

    val msg = PropertyGetMessage(
        "cos",
        null,
        Name.of("demo")
    )

//    println(msg.toMeta().toJson())

    sendEndpoint.send(MagixMessage(
        "controls-kt",
        msg.toMeta().toJson(),
        sourceEndpoint = "controls-kt"
    ))
    sendEndpoint.close()
    println("message sent")
}