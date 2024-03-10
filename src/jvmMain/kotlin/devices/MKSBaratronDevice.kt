package devices

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
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

        private suspend fun buildTCPConnection(hostname: String, port: Int): Socket {
            println("Connecting to port $hostname:$port")
            return aSocket(ActorSelectorManager(Dispatchers.IO)).tcp()
                .connect(InetSocketAddress(hostname, port))
        }

        suspend fun send() {

        }
        override suspend fun IMKSBaratronDevice.onOpen() {
            val socket = buildTCPConnection(hostname, port)
            val input = socket.openReadChannel()
            println("---------- connected")
            val sendChannel = socket.openWriteChannel(autoFlush = true)
            while (true) {
                sendChannel.writeFully("AV$channel\r".toByteArray())
                val reading = input.readUTF8Line()
                if (reading.isNullOrEmpty()) {
                    //                invalidateState("connection");
                    println("No connection")
                }
                val res = java.lang.Double.parseDouble(reading)
                if (res <= 0) {
                    println("Non positive")
                } else {
                    write(device_pressure, res)
                }
                //write(device_pressure, reading!!)
            }
        }
    }
}