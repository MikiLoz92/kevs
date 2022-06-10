plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    idea
}

kotlin {

    sourceSets {
        val commonMain by getting
        val commonTestFixtures by creating {
            dependsOn(commonMain)
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val commonTest by getting {
            dependsOn(commonTestFixtures)
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }

    jvm {
        defaultConfiguration()
        compilations {
            val main by getting
            val testFixtures by compilations.creating {
                defaultSourceSet {
                    //kotlin.srcDir("src/jvmTestFixtures")
                    dependsOn(sourceSets.getByName("commonTestFixtures"))
                    dependencies {
                        implementation(main.compileDependencyFiles)
                    }
                }
            }
        }
    }

    js(BOTH) {
        browser()
    }

    val kotlinCoroutinesVersion: String by project
    val kotlinxSerializationVersion: String by project

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutinesVersion")
            }
        }
        val jvmMain by getting
        /*val jvmTestFixtures by creating {
            kotlin.srcDir("src/jvmTestFixtures")
            dependsOn(commonTestFixtures)
        }*/
        val jvmTest by getting {
            dependencies {
                implementation("org.awaitility:awaitility-kotlin:4.1.1")
            }
        }
        val jsMain by getting
        val jsTest by getting
    }


    /*jvm {
        compilations {
            val main by getting {
                kotlinSourceSets.add(kotlin.sourceSets["commonTestFixtures"])
            }
        }
    }*/
}

val testFixturesJar = tasks.register<Jar>("fixturesJar") {
    //archiveClassifier.set("test")
    //from(kotlin.sourceSets["commonTestFixtures"].kotlin)
    from(kotlin.jvm().compilations["testFixtures"].output)
}

val testFixtures by configurations.creating {
    extendsFrom(configurations.commonMainImplementation.get())
}

artifacts {
    add("testFixtures", testFixturesJar)
}


idea {
    module {
        sourceDirs.remove(file("src/commonTestFixtures/kotlin"))
        sourceDirs.remove(file("src/jvmTestFixtures/kotlin"))
        //testSourceDirs = testSourceDirs + file("src/commonTestFixtures/kotlin") + file("src/jvmTestFixtures/kotlin")
        testSourceDirs.add(file("src/commonTestFixtures/kotlin"))
        testSourceDirs.add(file("src/jvmTestFixtures/kotlin"))
        /*sourceDirs -= file('src/integrationTest/java')
        testSourceDirs += file('src/integrationTest/java')*/
    }
}
