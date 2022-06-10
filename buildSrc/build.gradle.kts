plugins {
    `kotlin-dsl`
    //kotlin("multiplatform")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
    implementation(gradleApi())
}
