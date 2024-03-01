package commands

import devices.ISinCosDevice
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.toKotlinLocalDateTime
import storage.StorageClient
import java.time.temporal.ChronoUnit

// Fetch last 20 seconds of history
suspend fun main() = coroutineScope {
    val start = java.time.LocalDateTime.now().minus(20, ChronoUnit.SECONDS).toKotlinLocalDateTime()
    val client = StorageClient(CIO)
    val out = client.read("demo", ISinCosDevice.sin, start=start)
    println(out)
}
