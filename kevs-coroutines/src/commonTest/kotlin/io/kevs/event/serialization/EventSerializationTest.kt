package io.kevs.event.serialization

import io.kevs.annotation.Event
import io.kevs.annotation.EventIdentifier
import io.kevs.station.EventDispatcher
import io.kevs.station.EventCollector
import io.kevs.station.impl.SerializerEventStation
import io.kevs.stream.EventReceiveStream
import io.kevs.stream.EventTransmitStream
import io.kevs.stream.addEventListener
import io.kevs.stream.impl.DefaultEventReceiveStream
import io.kevs.stream.impl.DefaultEventTransmitStream
import io.kevs.stream.impl.RunOnThreadPoolEventReceiveStream
import io.kevs.stream.impl.sendEventAsync
import kotlin.test.Test

class EventSerializationTest {

    @Event
    @EventIdentifier("test.event")
    private data class TestEvent(val someData: String)

    @Test
    fun `it should serialize and deserialize an event`() {
        val event = TestEvent("dummy")
        val dummyStation = SerializerEventStation()
        val tx = DefaultEventTransmitStream.Companion.Builder()
                .addDispatcher(dummyStation as EventDispatcher)
                .build()
        val rx = DefaultEventReceiveStream.Companion.Builder()
                .addCollector(dummyStation as EventCollector)
                .build()

        rx.addEventListener<TestEvent> { assert(it.someData == event.someData) }
        tx.sendEvent(event)
    }


}
