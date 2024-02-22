package devices

import kotlinx.coroutines.launch
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
import kotlin.time.Duration.Companion.seconds

class SinCosDevice(context: Context, meta: Meta) : DeviceBySpec<SinCosDevice>(SinCosDevice, context, meta) {
    var timeScaleState = 5000.0
    var sinScaleState = 1.0
    var cosScaleState = 1.0

    fun time(): Instant = Instant.now()

    fun sinValue(): Double {
        return  sin(time().toEpochMilli().toDouble() / timeScaleState) * sinScaleState
    }

    fun cosValue(): Double = cos(time().toEpochMilli().toDouble() / timeScaleState) * cosScaleState

    companion object : DeviceSpec<SinCosDevice>(), Factory<SinCosDevice> {

        override fun build(context: Context, meta: Meta) = SinCosDevice(context, meta)

        // register virtual properties based on actual object state
        val timeScale by mutableProperty(MetaConverter.double, SinCosDevice::timeScaleState) {
//            metaDescriptor {
//                type(ValueType.NUMBER)
//            }
//            info = "Real to virtual time scale"
        }

        val sinScale by mutableProperty(MetaConverter.double, SinCosDevice::sinScaleState)
        val cosScale by mutableProperty(MetaConverter.double, SinCosDevice::cosScaleState)

        val sin by doubleProperty { sinValue() }
        val cos by doubleProperty { cosValue() }

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

        override suspend fun SinCosDevice.onOpen() {

            launch {
                read(sinScale)
                read(cosScale)
                read(timeScale)
            }

            doRecurring(10.seconds) {
                read(sin)
//            launch {
//                println("sin: ${read(sin)}; cos: ${read(cos)}; coords ${read(coordinates)}")
            }
        }
    }
}