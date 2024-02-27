package commands

import devices.SinCosDevice
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.toKotlinLocalDateTime
import storage.StorageClient
import java.time.temporal.ChronoUnit

// Fetch last 30 seconds of history
suspend fun main() = coroutineScope {
    val start = java.time.LocalDateTime.now().minus(20, ChronoUnit.SECONDS).toKotlinLocalDateTime()
    val client = StorageClient()
    val out = client.read("demo", SinCosDevice.sin, start=start)
    println(out)
}