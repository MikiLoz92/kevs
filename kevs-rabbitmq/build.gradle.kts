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
                implementation(project(":kevs-core"))
                implementation(project(":kevs-coroutines"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
            }
        }
        val commonTest by getting
        val jvmMain by getting {
            dependencies {
                implementation("com.rabbitmq:amqp-client:$amqpClientVersion")
                implementation("com.google.guava:guava:31.0.1-jre")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":kevs-core", configuration = "testFixtures"))
                implementation(project(":kevs-coroutines"))
                implementation("org.awaitility:awaitility-kotlin:4.1.1")
                implementation("org.junit.jupiter:junit-jupiter:5.8.1")
                implementation("org.testcontainers:testcontainers:1.16.2")
                implementation("org.testcontainers:junit-jupiter:1.16.2")
                implementation("com.google.guava:guava:31.0.1-jre")
            }
        }
    }
}
