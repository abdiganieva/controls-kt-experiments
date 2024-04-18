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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.nio.charset.Charset
import java.util.regex.Pattern
import kotlin.text.Charsets.US_ASCII
import space.kscience.controls.ports.*
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.int
import space.kscience.dataforge.meta.string
import space.kscience.controls.ports.*
import java.net.ConnectException

interface IMeradatVacDevice: Device {
    //var CONNECTED_STATE: String
    fun time(): Instant = Clock.System.now()
    var pressure: Double
    var hostname: String
    var port: Int
    var address: Int

}
class MeradatVacDevice(context: Context, meta: Meta): DeviceBySpec<IMeradatVacDevice>(
Companion, context, meta), IMeradatVacDevice {

    //override var CONNECTED_STATE: String = TODO()
    override var pressure: Double = 0.0
    override var hostname: String = "192.168.111.33"
    override var port: Int = 4003
    override var address: Int = 1

    companion object : DeviceSpec<IMeradatVacDevice>(), Factory<MeradatVacDevice> {

        private const val REQUEST = "0300000002"
        var sendChannel: ByteWriteChannel? = null
        var readChannel: ByteReadChannel? = null

        override fun build(context: Context, meta: Meta): MeradatVacDevice = MeradatVacDevice(context, meta)

        val device_pressure by mutableProperty(MetaConverter.double, IMeradatVacDevice::pressure)


        private suspend fun buildTCPConnection(hostname: String, port: Int): Socket {
            println("Connecting to port $hostname:$port")
            return aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
                .connect(InetSocketAddress(hostname, port))
        }


        private fun calculateLRC(inputString: String): String {
            /*
         * String is Hex String, need to convert in ASCII.
         */
            val bytes = BigInteger(inputString, 16).toByteArray()
            val checksum = bytes.sumOf { it.toInt() }
            var value = Integer.toHexString(-checksum)
            value = value.substring(value.length - 2).uppercase()
            if (value.length < 2) {
                value = "0$value"
            }

            return value
        }

        private suspend fun sendAndWait(query: String, predicate: (String) -> Boolean = { true }): String {
            sendChannel?.writeFully(query.toByteArray())
            var reading: String? = null
            while (reading.isNullOrEmpty() || !predicate(reading)) {
                reading = readChannel?.readUTF8Line()
            }
            return reading
        }

        override suspend fun IMeradatVacDevice.onOpen() {
            launch {
                var socket = buildTCPConnection(hostname, port)
                readChannel = socket.openReadChannel()
                sendChannel = socket.openWriteChannel(autoFlush = true)
                println("---------- connected")
                val requestBase: String = String.format(":%02d", address)
                val dataStr = requestBase.substring(1) + REQUEST
                val query = requestBase + REQUEST + calculateLRC(dataStr) + "\r\n" // ":010300000002FA\r\n";
                val response: Pattern = Pattern.compile(requestBase + "0304(\\w{4})(\\w{4})..") // removed \r\n from the end of the line idk why but it works now
                while (true) {
                    try {
                        Thread.sleep(1000)
                        val receivedData = sendAndWait(query) { phrase -> phrase.startsWith(requestBase) }

                        val match = response.matcher(receivedData)

                        if (match.matches()) {
                            val base = Integer.parseInt(match.group(1), 16).toDouble() / 10.0
                            var exp = Integer.parseInt(match.group(2), 16)
                            if (exp > 32766) {
                                exp -= 65536
                            }
                            var res = BigDecimal.valueOf(base * Math.pow(10.0, exp.toDouble()))
                            res = res.setScale(4, RoundingMode.CEILING)
                            println("--- CORRECT! --- $res")
                            write(device_pressure, res.toDouble())
                        } else {
                            println("Wrong answer: $receivedData")
                        }
                    } catch (e: ConnectException) {
                        socket.close()
                        socket = buildTCPConnection(hostname, port)
                        readChannel = socket.openReadChannel()
                        sendChannel = socket.openWriteChannel(autoFlush = true)
                        println("---------- reconnected")
                    }
                    //write(device_pressure, reading!!)
                }
            }
        }    
    }
}