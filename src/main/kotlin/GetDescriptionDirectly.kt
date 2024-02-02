import devices.SinCosDevice
import kotlinx.coroutines.coroutineScope
import space.kscience.controls.api.GetDescriptionMessage
import space.kscience.controls.api.toMeta
import space.kscience.controls.manager.DeviceManager
import space.kscience.controls.manager.respondMessage
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.toJson
import space.kscience.dataforge.names.Name

/// Create demo device and Magix server
//  and connect to each other inside one program
suspend fun main(): Unit = coroutineScope {


    val context = Context("clientContext") {
        plugin(DeviceManager)
    }
    val device = SinCosDevice.build(context, Meta.EMPTY)
    val descReq =  GetDescriptionMessage()
    val descResp = device.respondMessage(Name.of("demo"), descReq)

    println(descResp?.toMeta()?.toJson())
}

