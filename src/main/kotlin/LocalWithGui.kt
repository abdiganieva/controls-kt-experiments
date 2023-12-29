package org.example.localgui

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import space.kscience.controls.api.Device
import space.kscience.controls.api.metaDescriptor
import space.kscience.controls.manager.DeviceManager
import space.kscience.controls.manager.install
import space.kscience.controls.spec.*
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Factory
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.ValueType
import space.kscience.dataforge.meta.descriptors.value
import space.kscience.dataforge.meta.transformations.MetaConverter
import java.time.Instant
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

import space.kscience.controls.server.startDeviceServer

interface IDemoDevice: Device {
    var timeScaleState: Double
    var sinScaleState: Double
    var cosScaleState: Double

    fun time(): Instant = Instant.now()
    fun sinValue(): Double
    fun cosValue(): Double
}

class DemoDevice(context: Context, meta: Meta) : DeviceBySpec<IDemoDevice>(Companion, context, meta), IDemoDevice {
    override var timeScaleState = 5000.0
    override var sinScaleState = 1.0
    override var cosScaleState = 1.0

    override fun sinValue(): Double {
        return  sin(time().toEpochMilli().toDouble() / timeScaleState) * sinScaleState
    }

    override fun cosValue(): Double = cos(time().toEpochMilli().toDouble() / timeScaleState) * cosScaleState

    companion object : DeviceSpec<IDemoDevice>(), Factory<DemoDevice> {

        override fun build(context: Context, meta: Meta): DemoDevice = DemoDevice(context, meta)

        // register virtual properties based on actual object state
        val timeScale by mutableProperty(MetaConverter.double, IDemoDevice::timeScaleState) {
            metaDescriptor {
                type(ValueType.NUMBER)
            }
            info = "Real to virtual time scale"
        }

        val sinScale by mutableProperty(MetaConverter.double, IDemoDevice::sinScaleState)
        val cosScale by mutableProperty(MetaConverter.double, IDemoDevice::cosScaleState)

        val sin by doubleProperty(read = IDemoDevice::sinValue)
        val cos by doubleProperty(read = IDemoDevice::cosValue)

        val coordinates by metaProperty(
            descriptorBuilder = {
                metaDescriptor {
                    value("time", ValueType.NUMBER)
                }
            }
        ) {
            Meta {
                "time" put time().toEpochMilli()
                "x" put read(sin)
                "y" put read(cos)
            }
        }


        val resetScale by unitAction {
            write(timeScale, 5000.0)
            write(sinScale, 1.0)
            write(cosScale, 1.0)
        }

        override suspend fun IDemoDevice.onOpen() {
            println("DemoDevice opened")
            launch {
                read(sinScale)
                read(cosScale)
                read(timeScale)
            }
            doRecurring(50.milliseconds) {
                read(sin)
                read(cos)
                read(coordinates)
            }
        }
    }
}

suspend fun main() {

    val context = Context()
    val meta = Meta.EMPTY

    val device = DemoDevice.build(context, meta)
    val manager = DeviceManager()

    // register device and open it
    manager.install("demo", device)
    // just register device
//    deviceManager.registerDevice(NameToken("device"), device);

    GlobalScope.launch {
        startDeviceServer(manager)
    }

    while (true) {
        delay(1000)
        println("1 sec passed")
    }
}