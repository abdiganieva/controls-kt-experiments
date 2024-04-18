package devices

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import space.kscience.controls.api.Device
import space.kscience.controls.spec.*
import space.kscience.dataforge.meta.MetaConverter
import kotlin.time.Duration.Companion.seconds
interface IMeradatVacDevice: Device {
    var hostname: String
    var port: Int
    var address: Int

    suspend fun pressureValue(): Double
    fun time(): Instant = Clock.System.now()
    companion object : DeviceSpec<IMeradatVacDevice>() {

        val pressure by doubleProperty { pressureValue() }

        override suspend fun IMeradatVacDevice.onOpen() {
            println("--- before doRec")
            doRecurring(1.seconds) {
                println("--- before reading")
                read(pressure)
                println("--- after reading")
            }
        }
    }
}