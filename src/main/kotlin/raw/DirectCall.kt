package raw

import devices.SinCosDevice
import kotlinx.coroutines.coroutineScope
import space.kscience.controls.api.GetDescriptionMessage
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

/// Примеры ручного формирования Magix сообщения и обработки его девайсом
suspend fun main(): Unit = coroutineScope {

    // инициализация системы с тестовым устройством
    val device = run {
        val context = Context("clientContext") {
            plugin(DeviceManager)
        }
        val manager = context.request(DeviceManager)
        val device = SinCosDevice.build(context, Meta.EMPTY)
        manager.install("demo", device)
        device
    }

    /// Пример 1. Получение property девайса
    run {
        // формирование запроса
        val cosReq = PropertyGetMessage(
            "cos",
            null,
            Name.of("demo")
        )

        // обработка запроса устройством
        val cosResp = device.respondMessage(Name.of("demo"), cosReq)

        println(cosResp?.toMeta()?.toJson())
    }


    // Пример 2. Получение Description девайса
    run {
        // формирование запроса
        val descReq =  GetDescriptionMessage()

        // обработка запроса устройством
        val descResp = device.respondMessage(Name.of("demo"), descReq)

        println(descResp?.toMeta()?.toJson())
    }
}

