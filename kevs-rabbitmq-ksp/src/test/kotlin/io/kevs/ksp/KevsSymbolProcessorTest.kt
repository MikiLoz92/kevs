package io.kevs.ksp

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspArgs
import com.tschuchort.compiletesting.kspIncremental
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import io.kevs.event.serialization.EventSerializer
import io.kevs.rabbitmq.ksp.KevsProcessorProvider
import io.kevs.station.EventCollector
import io.kevs.station.EventDispatcher
import io.kevs.station.impl.SerializerEventStation
import io.kevs.stream.impl.DefaultEventReceiveStream
import io.kevs.stream.impl.DefaultEventTransmitStream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class KevsSymbolProcessorTest {

    // TODO: This whole test class makes no sense! Needs to be repurposed...

    @Test
    fun `it should generate event serializer class`() = runTest {
        val eventSourceFile = """
            package io.kevs.ksp
            
            import io.kevs.annotation.Event
            import kotlinx.serialization.SerialName
            import kotlinx.serialization.Serializable
            
            @Event
            @Serializable
            @SerialName("test.event")
            class TestEvent(val someData: String)
        """.trimIndent()

        val eventSubscriberSourceFile = """
            package io.kevs.ksp
            
            import io.kevs.station.rabbitmq.annotation.SubscribeTo
            import io.kevs.station.rabbitmq.subscriber.EventSubscriber
            
            @SubscribeTo(TestEvent::class)
            class DoSomethingOnTestEventSubscriber : EventSubscriber<TestEvent> {
                override suspend fun invoke(event: TestEvent) {
                    println("Hey")
                }
            }
        """.trimIndent()

        /*val idealEventSubscriberSourceFile = """
            package io.kevs.ksp
            
            @Singleton
            @CoroutinesEventSubscriber(OtherSomethingDone::class)
            class DoSomethingOnOtherSomethingDoneEventSubscriber(
                @Named("exchange") private val exchangePublishTransmitStream: EventTransmitStream, 
            ) {
                override suspend fun invoke(event: OtherSomethingDone) {
                    // Do something with the event & publish another event
                    exchangePublishTransmitStream.publish(SomethingHasBeenDoneEvent())
                }
            }
        """.trimIndent()*/

        val kspArguments = mutableMapOf(
            "serialization.kotlinx" to "true",
            "io.kevs.serialization.kotlinx.create-serializer" to "true",
            "io.kevs.rabbitmq.event-subscribers.create" to "true",
            "io.kevs.rabbitmq.event-collectors.create" to "true",
            "io.kevs.rabbitmq.spring.create-configuration-bean" to "true",
            "io.kevs.rabbitmq.spring.configuration-bean-package" to "eos.connectivity.backend.v2.application.configuration",
            "io.kevs.rabbitmq.spring.configuration-bean-class" to "EventSubscribersConfiguration",
        )

        val sourceFiles = listOf(
            SourceFile.kotlin("TestEvent.kt", eventSourceFile),
            SourceFile.kotlin("DoSomethingOnTestEventSubscriber.kt", eventSubscriberSourceFile),
        )
        val compilationPass1 = KotlinCompilation().apply {
            sources = sourceFiles
            // TODO: Auto download from https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-serialization/1.6.21
            kotlincArguments = listOf("-Xplugin=$KotlinxSerializationPluginJarPath")
            inheritClassPath = true
            symbolProcessorProviders = listOf(KevsProcessorProvider())
            messageOutputStream = System.out
            kspIncremental = true
            kspArgs = kspArguments
        }

        val result = compilationPass1.compile()

        val compilationPass2 = KotlinCompilation().apply {
            kotlincArguments = listOf("-Xplugin=$KotlinxSerializationPluginJarPath")
            sources = compilationPass1.sources + compilationPass1.kspGeneratedSourceFiles
            inheritClassPath = true
            kspArgs = kspArguments
        }.compile()

        println(result.classLoader)
        val serializerClass =
            compilationPass2.classLoader.loadClass("eos.connectivity.backend.v2.application.configuration.EosKevsRabbitMqConfiguration")
        val clazz = serializerClass.constructors.first().newInstance()

        println(clazz)

    }
}

private val KotlinCompilation.kspGeneratedSourceFiles: List<SourceFile>
    get() = kspSourcesDir.resolve("kotlin")
            .walk()
            .filter { it.isFile }
            .map { SourceFile.fromPath(it.absoluteFile) }
            .toList()
