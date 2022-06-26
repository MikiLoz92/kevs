import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
    idea
    id("com.google.devtools.ksp")

    id("org.springframework.boot") version "2.7.0"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    kotlin("plugin.spring") version "1.6.21"
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
    arg("io.kevs.rabbitmq.event-subscribers.create", "true")
    arg("io.kevs.rabbitmq.event-collectors.create", "true")
    arg("io.kevs.rabbitmq.spring.create-configuration-bean", "true")
    arg("io.kevs.rabbitmq.spring.configuration-bean-package", "io.kevs.rabbitmq.springboot.example")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
    implementation(project(":kevs-core"))
    ksp(project(":kevs-ksp"))
    implementation(project(":kevs-rabbitmq"))
    ksp(project(":kevs-rabbitmq-ksp"))

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

kotlin {
    sourceSets["main"].apply {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
