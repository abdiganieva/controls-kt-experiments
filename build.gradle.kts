plugins {
    kotlin("multiplatform") version "1.9.22"
    // подключение compose-multiplatform - рекомендуемое средство визуализации (сейчас не используется)
    // id("org.jetbrains.compose") version "1.5.12"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
//    mavenLocal()
    maven("https://repo.kotlin.link")
     maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

val controlsVersion = "0.3.0"
val ktorVersion = "2.3.7"

kotlin {

    explicitApi = null
    sourceSets {
        commonMain {
            dependencies {
                // зависимости, необходимые для задания спеки девайса в common модуле
                implementation("space.kscience:controls-core:$controlsVersion")

                // зависимости, необходимые для StorageClient
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            }
        }

        jsMain {
            dependencies {
                // зависимости, необходимые для работы с Magix на Js клиенте
                implementation("space.kscience:controls-magix:$controlsVersion")
                implementation("space.kscience:magix-rsocket:$controlsVersion")

                // зависимости, необходимые для работы с StorageClient на Js клиенте
                implementation("io.ktor:ktor-client-js:$ktorVersion")

                // зависимости для построения графики с помощью compose-multiplatform (сейчас не используется)
                // implementation(compose.runtime)
                // implementation(compose.html.core)
            }
        }

        jvmMain {
            dependencies {
                // Зависимости для работы Magix сервера
                // смотреть актуальные версии здесь: https://maven.sciprog.center/#/kscience

                // создание девайса (можно не подключать, т.к. уже есть в зависимостях common)
                // implementation("space.kscience:controls-core:$controlsVersion")
                implementation("space.kscience:controls-server:$controlsVersion") // startDeviceServer
                implementation("space.kscience:magix-server:$controlsVersion") // startMagixServer
                implementation("space.kscience:controls-magix:$controlsVersion") // launchMagixService
                // клиентское подключение к Magix по RSocket (MagixEndpoint.rSocketWithWebSockets)
                implementation("space.kscience:magix-rsocket:$controlsVersion")

                // зависимости, необходимые для реализации storage
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }
    }

    js {
        browser()
        binaries.executable()
    }

    jvm()
}