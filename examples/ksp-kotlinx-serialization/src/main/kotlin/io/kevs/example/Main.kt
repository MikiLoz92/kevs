package io.kevs.example

import io.kevs.event.listener.ConditionalEventListener
import io.kevs.event.listener.WithMetadataEventListener
import io.kevs.event.metadata.EventMetadata
import io.kevs.event.metadata.EventWithMetadata
import io.kevs.event.serialization.KotlinxSerializationEventSerializer
import io.kevs.station.EventCollector
import io.kevs.station.EventDispatcher
import io.kevs.station.impl.SerializerEventStation
import io.kevs.stream.addEventListener
import io.kevs.stream.impl.DefaultEventTransmitStream
import io.kevs.stream.impl.RunOnExecutorEventReceiveStream
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import java.util.concurrent.Executors

fun main() = runBlocking {

    val event = EventWithMetadata(EventMetadata("wazzap"), TestEvent("something"))
    //val event = TestEvent("something")

    val receiveExecutor = Executors.newCachedThreadPool()
    val serializer = KotlinxSerializationEventSerializer(Json)
    val dummyStation = SerializerEventStation.builder()
            .serializer(serializer)
            .build()
    val tx = DefaultEventTransmitStream.builder()
            .addDispatcher(dummyStation as EventDispatcher)
            .build()
    val rx = RunOnExecutorEventReceiveStream.builder()
            .executor(receiveExecutor)
            .addCollector(dummyStation as EventCollector)
            .build()

    rx.addEventListener<TestEvent> { println(it.someData + "asdasd") }
    rx.addEventListener(object : ConditionalEventListener<TestEvent> {
        override fun shouldExecute(metadata: EventMetadata, event: TestEvent) = metadata.collectorId == "wazzap"
        override suspend fun invoke(event: TestEvent) {
            println(event.someData)
        }
    })
    rx.addEventListener(object : ConditionalEventListener<TestEvent>, WithMetadataEventListener<TestEvent> {
        override fun shouldExecute(metadata: EventMetadata, event: TestEvent) = metadata.collectorId == "wazzap"
        override suspend fun invoke(metadata: EventMetadata, event: TestEvent) {
            println(metadata.collectorId)
        }
    })
    rx.addEventListener(WithMetadataEventListener<TestEvent> { md, ev ->
        println(md.collectorId)
        println(ev.someData)
    })
    tx.sendEvent(event)

    delay(1000)
    receiveExecutor.shutdownNow()
    Unit
}
