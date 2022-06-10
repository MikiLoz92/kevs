plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
    idea
    id("com.google.devtools.ksp")
}

group = "io.kevs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val kotlinVersion: String by project
val kotlinCoroutinesVersion: String by project
val kotlinxSerializationVersion: String by project

ksp {
    arg("io.kevs.serialization.kotlinx.create-serializer", "true")
    arg("io.kevs.serialization.kotlinx.class-discriminator", "_eventType")
    arg("io.kevs.serialization.kotlinx.event-identifier-annotation", "io.kevs.annotation.EventIdentifier")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
    implementation(project(":kevs-core"))
    ksp(project(":kevs-ksp"))
}

kotlin {
    sourceSets["main"].apply {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}
