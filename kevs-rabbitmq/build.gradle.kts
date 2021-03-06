plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {

    jvm { defaultConfiguration() }

    val kotlinCoroutinesVersion: String by project
    val kotlinxSerializationVersion: String by project
    val amqpClientVersion: String by project

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Moving this to JVM generates errors: https://youtrack.jetbrains.com/issue/KT-49877/MISSINGDEPENDENCYCLASS-for-Coroutines-Builders-in-IDE
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
                implementation("net.pearx.kasechange:kasechange:1.3.0")
                api(project(":kevs-coroutines")) // API is needed because it must bring coroutines module into scope
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(project(":kevs-core"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
                api("com.rabbitmq:amqp-client:$amqpClientVersion")
                implementation("io.micrometer:micrometer-core:1.9.0")
                implementation("com.google.guava:guava:31.0.1-jre")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":kevs-core", configuration = "testFixtures"))
                implementation("org.awaitility:awaitility-kotlin:4.1.1")
                implementation("org.junit.jupiter:junit-jupiter:5.8.1")
                implementation("org.testcontainers:testcontainers:1.16.2")
                implementation("org.testcontainers:junit-jupiter:1.16.2")
                implementation("com.google.guava:guava:31.0.1-jre")
            }
        }
    }
}
