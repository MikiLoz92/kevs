import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

fun KotlinMultiplatformExtension.defaultConfiguration() {
    jvm {
        defaultConfiguration()
    }
}

fun KotlinJvmTarget.defaultConfiguration() {
    compilations.all {
        kotlinOptions.jvmTarget = "1.8"
    }
    // TODO: withJava() errors when mixed custom compilations, investigate why...
    //withJava()
    testRuns["test"].executionTask.configure {
        useJUnitPlatform()
    }
}
