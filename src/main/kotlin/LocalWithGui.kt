import devices.SinCosDevice
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import space.kscience.controls.manager.DeviceManager
import space.kscience.controls.manager.install
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.meta.Meta

import space.kscience.controls.server.startDeviceServer

suspend fun main() {

    val context = Context()
    val meta = Meta.EMPTY

    val device = SinCosDevice.build(context, meta)
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