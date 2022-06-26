pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
    plugins {
        val kotlinVersion: String by settings
        val kspVersion: String by settings
        kotlin("multiplatform") version kotlinVersion
        kotlin("jvm") version kotlinVersion apply false
        kotlin("plugin.serialization") version kotlinVersion
        id("com.google.devtools.ksp") version kspVersion apply false
    }
}

include(":kevs-core")
include(":kevs-coroutines")
include(":kevs-ksp")
include(":kevs-rabbitmq")
include(":kevs-rabbitmq-ksp")
include(":examples:ksp-kotlinx-serialization")
include(":examples:rabbitmq-springboot")
