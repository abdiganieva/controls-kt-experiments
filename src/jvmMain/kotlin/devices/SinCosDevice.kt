package devices

import kotlinx.coroutines.launch
import space.kscience.controls.spec.*
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Factory
import space.kscience.dataforge.meta.Meta
import java.time.Instant
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration.Companion.seconds

class SinCosDevice(context: Context, meta: Meta) : DeviceBySpec<SinCosDevice>(ISinCosDevice, context, meta), ISinCosDevice {
    override var timeScaleState = 5000.0
    override var sinScaleState = 1.0
    override var cosScaleState = 1.0

    fun time(): Instant = Instant.now()

    override fun sinValue(): Double = sin(time().toEpochMilli().toDouble() / timeScaleState) * sinScaleState
    override fun cosValue(): Double = cos(time().toEpochMilli().toDouble() / timeScaleState) * cosScaleState

    companion object : DeviceSpec<ISinCosDevice>(), Factory<SinCosDevice> {
        override fun build(context: Context, meta: Meta) = SinCosDevice(context, meta)
        override suspend fun ISinCosDevice.onOpen() {
            println("Im here")
            launch {
                read(ISinCosDevice.sinScale)
                read(ISinCosDevice.cosScale)
                read(ISinCosDevice.timeScale)
            }
            println("tick")
            doRecurring(1.seconds) {
                println("tick")
                read(ISinCosDevice.sin)
            }
        }
    }
}