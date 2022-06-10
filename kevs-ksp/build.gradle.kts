plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "io.kevs" 
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions.freeCompilerArgs = listOf(
        "-opt-in=kotlin.RequiresOptIn",
        "-opt-in=io.kevs.annotation.InternalKevsApi",
        "-opt-in=com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview",
    )
}

tasks.withType<Test> {
    testLogging {
        showStandardStreams = true
        showStackTraces = true
    }
}

val kotlinVersion: String by project
val kotlinCoroutinesVersion: String by project
val kotlinxSerializationVersion: String by project

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.6.21-1.0.6")
    implementation("com.squareup:kotlinpoet:1.10.2")
    implementation("com.squareup:kotlinpoet-ksp:1.10.2")
    implementation(project(":kevs-core"))

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutinesVersion")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.8")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.8")
}
