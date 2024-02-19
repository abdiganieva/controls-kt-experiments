plugins {
    kotlin("jvm") version "1.9.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.kotlin.link")
}

//val controlsVersion = "0.3.0-dev-6-local"
val controlsVersion = "0.3.0-dev-6-16-02-24-local"
//val controlsVersion = "0.3.0-dev-4"
val ktorVersion = "2.3.7"

dependencies {

    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.10")

//    смотреть зависисмости и актуальные версии здесь:
//    https://maven.sciprog.center/#/kscience
    implementation("space.kscience:controls-server:$controlsVersion")
    implementation("space.kscience:controls-core:$controlsVersion")
    implementation("space.kscience:controls-magix:$controlsVersion")
    implementation("space.kscience:magix-server:$controlsVersion")
    implementation("space.kscience:magix-api:$controlsVersion")
    implementation("space.kscience:magix-rsocket:$controlsVersion")

    // web ui
    // TODO: почему надо подключать?
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")

    implementation("org.slf4j:slf4j-log4j12:2.0.9")

//    implementation(spclibs.logback.classic)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}