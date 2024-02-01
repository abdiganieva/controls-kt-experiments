package devices

import kotlinx.coroutines.launch
import space.kscience.controls.api.Device
import space.kscience.controls.api.metaDescriptor
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


interface ISinCosDevice: Device {
    var timeScaleState: Double
    var sinScaleState: Double
    var cosScaleState: Double

    fun time(): Instant = Instant.now()
    fun sinValue(): Double
    fun cosValue(): Double
}

class SinCosDevice(context: Context, meta: Meta) : DeviceBySpec<ISinCosDevice>(Companion, context, meta), ISinCosDevice {
    override var timeScaleState = 5000.0
    override var sinScaleState = 1.0
    override var cosScaleState = 1.0

    override fun sinValue(): Double {
        return  sin(time().toEpochMilli().toDouble() / timeScaleState) * sinScaleState
    }

    override fun cosValue(): Double = cos(time().toEpochMilli().toDouble() / timeScaleState) * cosScaleState

    companion object : DeviceSpec<ISinCosDevice>(), Factory<SinCosDevice> {

        override fun build(context: Context, meta: Meta) = SinCosDevice(context, meta)

        // register virtual properties based on actual object state
        val timeScale by mutableProperty(MetaConverter.double, ISinCosDevice::timeScaleState) {
//            metaDescriptor {
//                type(ValueType.NUMBER)
//            }
            info = "Real to virtual time scale"
        }

        val sinScale by mutableProperty(MetaConverter.double, ISinCosDevice::sinScaleState)
        val cosScale by mutableProperty(MetaConverter.double, ISinCosDevice::cosScaleState)

        val sin by doubleProperty(read = ISinCosDevice::sinValue)
        val cos by doubleProperty(read = ISinCosDevice::cosValue)

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

        override suspend fun ISinCosDevice.onOpen() {
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