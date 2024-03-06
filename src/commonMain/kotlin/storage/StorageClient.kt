package storage

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.plugins.contentnegotiation.*
import kotlinx.datetime.LocalDateTime
import space.kscience.controls.spec.DevicePropertySpec
import space.kscience.controls.spec.name
import space.kscience.dataforge.meta.Meta

class StorageClient<T: HttpClientEngineConfig>(
    engineFactory: HttpClientEngineFactory<T>,
    private val port: Int = 8080
) {
    private val client = HttpClient(engineFactory) {
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun <T> read(
            device: String, propertySpec: DevicePropertySpec<*, T>,
            start: LocalDateTime? = null,
            end: LocalDateTime? = null
        ): List<Pair<LocalDateTime, T>> {
        val res: List<Pair<LocalDateTime, Meta>> = client.get("http://localhost:${this@StorageClient.port}/history/${device}/${propertySpec.name}") {
            url {
                if (start != null) parameters.append("start", start.toString())
                if (end != null) parameters.append("end", end.toString())
            }
        }.body()

        return res.map {
            Pair(
                it.first,
                propertySpec.converter.read(it.second)
            )
        }
    }
}