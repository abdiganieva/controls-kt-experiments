package storage

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.*
import kotlinx.serialization.json.Json
import space.kscience.controls.api.PropertyChangedMessage
import space.kscience.controls.manager.DeviceManager
import space.kscience.controls.manager.hubMessageFlow
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.toJson

import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries

// Simple device attributes storage implementation
// This function will store all changed properties for devices in device manager in tsv format
// And provide REST API for retrieving history data
// NOTE: storage have no optimisation
// NOTE: storage works only with flat properties (Int, Double etc., structures not supported)
 suspend fun DeviceManager.startStorage(port: Int, dataRoot: Path): ApplicationEngine {
    this.hubMessageFlow(this.context).filterIsInstance<PropertyChangedMessage>().onEach {
        val time = it.time!!.toLocalDateTime(timeZone = TimeZone.currentSystemDefault())
        val todayRoot = Path(
            dataRoot.toString(),
            "${time.year}-${"%02d".format(time.monthNumber)}-${"%02d".format(time.dayOfMonth)}",
            "${it.sourceDevice}")
        todayRoot.createDirectories()
        val tsvFile = File(Path(todayRoot.toString(), "${it.property}.tsv").toString())
        tsvFile.appendText("${time.time}\t${it.value.toJson()}\n") // TODO: round to milliseconds
    }.launchIn(this.context)

    return embeddedServer(CIO, port = port) {
        install(ContentNegotiation) {
            json()
        }

        routing {
            get("/history/{device}/{property}") {
                val device = call.parameters["device"].toString()
                val property = call.parameters["property"].toString()

                val start = if(call.request.queryParameters.contains("start")) {
                    LocalDateTime.parse(call.request.queryParameters["start"]!!)
                } else {
                    java.time.LocalDate.now().atStartOfDay()!!.toKotlinLocalDateTime()
                }

                val end = if(call.request.queryParameters.contains("end")) {
                    LocalDateTime.parse(call.request.queryParameters["end"]!!)
                } else {
                    java.time.LocalDateTime.now()!!.toKotlinLocalDateTime()
                }

                val timeSeries = emptyList<Pair<LocalDateTime, Meta>>().toMutableList()
                dataRoot.listDirectoryEntries().sorted().forEach {
                    val date = LocalDateTime(LocalDate.parse(it.fileName.toString()), LocalTime(0,0,0))
                    val startDate = LocalDateTime(start.date, LocalTime(0,0,0))
                    if(date in startDate..end) {
                        val dayRecord = Path(it.toString(), device, "${property}.tsv")
                        if(dayRecord.exists()) {
                            File(dayRecord.toString()).readLines().forEach {
                                val (timeRaw, valueRaw) = it.split("\t")
                                val timestamp = LocalDateTime(date.date, LocalTime.parse(timeRaw))
                                if (timestamp in start..end) {
                                    val json = Json.decodeFromString<Meta>(valueRaw)
                                    val entry = Pair(timestamp, json)
                                    timeSeries += entry
                                }
                            }
                        }
                    }
                }
                call.respond(timeSeries)
            }
        }
    }.start()
}