package devices

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import space.kscience.controls.api.Device
import space.kscience.controls.spec.DeviceBySpec
import space.kscience.controls.spec.DeviceSpec
import space.kscience.controls.spec.mutableProperty
import space.kscience.controls.spec.write
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Factory
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaConverter

interface IMKSBaratronDevice: Device {
    //var CONNECTED_STATE: String
    fun time(): Instant = Clock.System.now()
    var channel: String
    var pressure: Double
    var hostname: String
    var port: Int

}

class MKSBaratronDevice(context: Context, meta: Meta): DeviceBySpec<IMKSBaratronDevice>(
    Companion, context, meta), IMKSBaratronDevice {

    //override var CONNECTED_STATE: String = TODO()
    override var pressure: Double = 0.0
    override var hostname: String = "192.168.111.33"
    override var port: Int = 4004
    override var channel: String = "1"


    companion object : DeviceSpec<IMKSBaratronDevice>(), Factory<MKSBaratronDevice> {
        override fun build(context: Context, meta: Meta): MKSBaratronDevice = MKSBaratronDevice(context, meta)

        val device_pressure by mutableProperty(MetaConverter.double, IMKSBaratronDevice::pressure)

        var sendChannel: ByteWriteChannel? = null
        var readChannel: ByteReadChannel? = null

        private suspend fun buildTCPConnection(hostname: String, port: Int): Socket {
            println("Connecting to port $hostname:$port")
            return aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
                .connect(InetSocketAddress(hostname, port))
        }

        private suspend fun sendAndWait(query: String, predicate: (String) -> Boolean = { true }): String {
            sendChannel?.writeFully(query.toByteArray())
            var reading: String? = null
            while (reading.isNullOrEmpty() || !predicate(reading)) {
                reading = readChannel?.readUTF8Line()
            }
            return reading
        }


        override suspend fun IMKSBaratronDevice.onOpen() {
            val socket = buildTCPConnection(hostname, port)
            readChannel = socket.openReadChannel()
            println("---------- connected")
            sendChannel = socket.openWriteChannel(autoFlush = true)

            while (true) {
                val receivedData = sendAndWait("AV$channel\r")
                /*
                if (reading.isEmpty()) {
                    //                invalidateState("connection");
                    println("No connection")
                }
                */
                val res = java.lang.Double.parseDouble(receivedData)
                if (res <= 0) {
                    println("Non positive")
                } else {
                    println("RESULT: $res")
                    write(device_pressure, res)
                }
                //write(device_pressure, reading!!)
            }
        }
    }
}