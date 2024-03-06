package devices

import space.kscience.controls.api.Device
import space.kscience.controls.api.metaDescriptor
import space.kscience.controls.spec.*
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaConverter

interface ISinCosDevice : Device {
    var timeScaleState: Double
    var sinScaleState: Double
    var cosScaleState: Double

    fun sinValue(): Double
    fun cosValue(): Double

    companion object : DeviceSpec<ISinCosDevice>() {

        val timeScale by mutableProperty(MetaConverter.double, ISinCosDevice::timeScaleState)
        val sinScale by mutableProperty(MetaConverter.double, ISinCosDevice::sinScaleState)
        val cosScale by mutableProperty(MetaConverter.double, ISinCosDevice::cosScaleState)

        val sin by doubleProperty { sinValue() }
        val cos by doubleProperty { cosValue() }

        val coordinates by metaProperty(
            descriptorBuilder = {
                metaDescriptor {
//                    value("time", ValueType.NUMBER)
                }
            }
        ) {
            Meta {
//                "time" put time().toEpochMilli()
                "x" put read(sin)
                "y" put read(cos)
            }
        }

        val resetScale by unitAction {
            write(timeScale, 5000.0)
            write(sinScale, 1.0)
            write(cosScale, 1.0)
        }

    }
}