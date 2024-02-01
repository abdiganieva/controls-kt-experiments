import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import space.kscience.magix.api.MagixEndpoint
import space.kscience.magix.rsocket.rSocketWithTcp

/// Simple Magix logger that prints all incoming messages to console
suspend fun main(): Unit = coroutineScope {
    val logEndpoint = MagixEndpoint.rSocketWithTcp(
        "localhost", 7778
    )
    logEndpoint.subscribe().onEach {
        println(it)
    }.launchIn(this)
}