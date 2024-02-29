import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import space.kscience.magix.api.MagixEndpoint
import space.kscience.magix.rsocket.rSocketWithWebSockets

/// Simple Magix logger that prints all incoming messages to console
suspend fun main(): Unit = coroutineScope {
    val logEndpoint = MagixEndpoint.rSocketWithWebSockets(
        "localhost", 7777
    )
    logEndpoint.subscribe().onEach {
        println(it)
    }.launchIn(this)
}