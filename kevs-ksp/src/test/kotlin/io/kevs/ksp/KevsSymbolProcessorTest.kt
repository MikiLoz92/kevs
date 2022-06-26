package io.kevs.ksp

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.PluginOption
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspArgs
import com.tschuchort.compiletesting.kspIncremental
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.kevs.event.serialization.EventSerializer
import io.kevs.station.EventCollector
import io.kevs.station.EventDispatcher
import io.kevs.station.impl.SerializerEventStation
import io.kevs.stream.addEventListener
import io.kevs.stream.impl.DefaultEventReceiveStream
import io.kevs.stream.impl.DefaultEventTransmitStream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.cli.js.loadPluginsForTests
import java.io.File
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KevsSymbolProcessorTest {

    @Test
    fun `it should generate event serializer class`() = runTest {
        val sourceFile = """
            package io.kevs.ksp
            
            import io.kevs.annotation.Event
            import kotlinx.serialization.SerialName
            import kotlinx.serialization.Serializable
            
            @Event
            @Serializable
            @SerialName("test.event")
            class TestEvent(val someData: String)
    """.trimIndent()

        val source = SourceFile.kotlin("TestEvent.kt", sourceFile)
        val compilationPass1 = KotlinCompilation().apply {
            sources = listOf(source)
            // TODO: Auto download from https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-serialization/1.6.21
            kotlincArguments = listOf("-Xplugin=$KotlinxSerializationPluginJarPath")
            inheritClassPath = true
            symbolProcessorProviders = listOf(KevsProcessorProvider())
            messageOutputStream = System.out
            kspIncremental = true
            kspArgs = mutableMapOf(
                "serialization.kotlinx" to "true",
                "io.kevs.serialization.kotlinx.create-serializer" to "true"
            )
        }

        val result = compilationPass1.compile()

        val compilationPass2 = KotlinCompilation().apply {
            kotlincArguments = listOf("-Xplugin=$KotlinxSerializationPluginJarPath")
            sources = compilationPass1.sources + compilationPass1.kspGeneratedSourceFiles
            inheritClassPath = true
            kspArgs = mutableMapOf(
                "serialization.kotlinx" to "true",
                "io.kevs.serialization.kotlinx.create-serializer" to "true"
            )
        }.compile()

        println(result.classLoader)
        val serializerClass = compilationPass2.classLoader.loadClass("io.kevs.event.serialization.KotlinxSerializationEventSerializer")
        val serializer = serializerClass.constructors.first().newInstance(Json) as EventSerializer

        val eventClass = compilationPass2.classLoader.loadClass("io.kevs.ksp.TestEvent")
        val event = eventClass.constructors.first().newInstance("some data")
        val dummyStation = SerializerEventStation.Companion.builder()
                .serializer(serializer)
                .build()
        val tx = DefaultEventTransmitStream.Companion.builder()
                .addDispatcher(dummyStation as EventDispatcher)
                .build()
        val rx = DefaultEventReceiveStream.Companion.builder()
                .addCollector(dummyStation as EventCollector)
                .build()

        rx.addEventListener(eventClass.kotlin) { println(it) }
        tx.sendEvent(event)

    }
}

private val KotlinCompilation.kspGeneratedSourceFiles: List<SourceFile>
    get() = kspSourcesDir.resolve("kotlin")
            .walk()
            .filter { it.isFile }
            .map { SourceFile.fromPath(it.absoluteFile) }
            .toList()
