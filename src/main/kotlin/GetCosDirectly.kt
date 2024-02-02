import devices.SinCosDevice
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import space.kscience.controls.api.PropertyGetMessage
import space.kscience.controls.api.toMeta
import space.kscience.controls.manager.DeviceManager
import space.kscience.controls.manager.install
import space.kscience.controls.manager.respondMessage
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.request
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.toJson
import space.kscience.dataforge.names.Name
import kotlin.time.Duration.Companion.seconds

/// Create demo device and Magix server
//  and connect to each other inside one program
suspend fun main(): Unit = coroutineScope {

    /// Создание менеджера устройства
    val context = Context("clientContext") {
        plugin(DeviceManager)
    }
    // Создание девайс менеджера напрямую не рекомендовано
    // TODO: писать предупреждение, если девайс менеджер создается напрямую
    val manager = context.request(DeviceManager)
    val device = SinCosDevice.build(context, Meta.EMPTY)
    // register device and open it
    manager.install("demo", device)

    val cosReq = PropertyGetMessage(
        "cos",
        null,
        Name.of("demo")
    )

    while (true) {
        val cosResp = device.respondMessage(Name.of("demo"), cosReq)
        println(cosResp?.toMeta()?.toJson())
        delay(1.seconds)
    }
}

