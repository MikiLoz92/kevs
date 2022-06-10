package io.kevs.example

import io.kevs.event.serialization.KotlinxSerializationEventSerializer
import io.kevs.station.EventCollector
import io.kevs.station.EventDispatcher
import io.kevs.station.impl.SerializerEventStation
import io.kevs.stream.addEventListener
import io.kevs.stream.impl.DefaultEventTransmitStream
import io.kevs.stream.impl.RunOnExecutorEventReceiveStream
import kotlinx.serialization.json.Json
import java.util.concurrent.Executors

fun main() {
    val event = TestEvent("something")

    val serializer = KotlinxSerializationEventSerializer(Json)
    val dummyStation = SerializerEventStation.Companion.builder()
            .serializer(serializer)
            .build()
    val tx = DefaultEventTransmitStream.Companion.builder()
            .addDispatcher(dummyStation as EventDispatcher)
            .build()
    val rx = RunOnExecutorEventReceiveStream.Companion.builder()
            .executor(Executors.newCachedThreadPool())
            .addCollector(dummyStation as EventCollector)
            .build()

    rx.addEventListener<TestEvent> { println(it.someData) }
    tx.sendEvent(event)
}
