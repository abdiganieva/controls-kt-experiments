package devices

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import space.kscience.controls.api.Device
import space.kscience.controls.spec.*
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Factory
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaConverter
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
interface ICM32Device: Device {
    //var CONNECTED_STATE: String
    fun time(): Instant = Clock.System.now()
    var pressure: Double
    var hostname: String
    var port: Int

}

class CM32Device(context: Context, meta: Meta): DeviceBySpec<ICM32Device>(
    Companion, context, meta), ICM32Device {

    //override var CONNECTED_STATE: String = TODO()
    override var pressure: Double = 0.0
    override var hostname: String = "127.0.0.1"
    override var port: Int = 9002


    companion object : DeviceSpec<ICM32Device>(), Factory<CM32Device> {
        override fun build(context: Context, meta: Meta): CM32Device = CM32Device(context, meta)

        val device_pressure by mutableProperty(MetaConverter.double, ICM32Device::pressure)

        suspend fun buildTCPConnection(hostname: String, port: Int): Socket {
            println("Connecting to port $hostname:$port")
            val socket = aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
                .connect(InetSocketAddress(hostname, port))
            return socket
        }

        suspend fun send() {

        }
        override suspend fun ICM32Device.onOpen() {
            val socket = buildTCPConnection(hostname, port)
            val input = socket.openReadChannel()
            println("---------- connected")
            val sendChannel = socket.openWriteChannel(autoFlush = true)
            sendChannel.writeFully("MES R PM 1\r\n".toByteArray())
            while (true) {
                val reading = input.readUTF8Line()
                val meas = input.readUTF8Line()?.substring(12, 16)?.toDouble()
                if (reading.isNullOrEmpty()) {
                    println("No signal")
                } else if (!reading.contains("PM1:mbar")) {
                    println("Wrong answer: $reading")
                } else if (reading.substring(14, 17) == "OFF") {
                    println("Off")
                } else {
                    println(reading.substring(14, 17) + reading.substring(19, 23))
                }
                //write(device_pressure, reading!!)
            }
        }
    }
}