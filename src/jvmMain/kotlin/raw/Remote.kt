package raw

import kotlinx.coroutines.coroutineScope
import space.kscience.controls.api.GetDescriptionMessage
import space.kscience.controls.api.PropertyGetMessage
import space.kscience.controls.api.toMeta
import space.kscience.dataforge.meta.toJson
import space.kscience.dataforge.names.Name
import space.kscience.magix.api.MagixEndpoint
import space.kscience.magix.api.MagixMessage
import space.kscience.magix.api.send
import space.kscience.magix.rsocket.rSocketWithWebSockets

/// Примеры ручного формирования Magix сообщений и отправки их в Magix
// NOTE: данный скрипт только отправляет сообщения. Для просмотра ответов от девайсов
// NOTE: для работы этого скрипта [Main.kt](../Main.kt) должен быть запущен
// нужно использовать [Logger.kt](../Logger.kt)
// TODO не работает (сообщения отправляются, но девайс не реагирует)
// TODO закрыть канал (и корутину)
// TODO отлавливать ответы
suspend fun main(): Unit = coroutineScope {

    // подключение в Magix по вебсокету
    val sendEndpoint = MagixEndpoint.rSocketWithWebSockets(
        "localhost", 7777
    )

//    val sendEndpoint = MagixEndpoint.rSocketWithTcp(
//        "localhost", 7778
//    )

    /// Пример 1. Запрос property девайса
    run {
        // формирование запроса
        val msg = PropertyGetMessage(
            "cos",
            null,
            Name.of("demo")
        )

        // отправка запроса в Magix
        sendEndpoint.send(MagixMessage(
            "controls-kt",
            msg.toMeta().toJson(),
            sourceEndpoint = "controls-kt",
            targetEndpoint = "controls-kt"
        ))
    }

    // Пример 2. Запрос Description девайса
    run {
        // формирование запроса
        val msg = GetDescriptionMessage()

        // отправка запроса в Magix
        sendEndpoint.send(
            MagixMessage(
                "controls-kt",
                msg.toMeta().toJson(),
                sourceEndpoint = "controls-kt",
                targetEndpoint = "controls-kt"
            )
        )
    }


    sendEndpoint.close()
    println("finished")
}

