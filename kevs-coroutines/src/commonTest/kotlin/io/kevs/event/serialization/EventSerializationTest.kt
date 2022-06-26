package io.kevs.event.serialization

import io.kevs.annotation.Event
import io.kevs.annotation.EventIdentifier
import io.kevs.station.EventCollector
import io.kevs.station.EventDispatcher
import io.kevs.station.impl.SerializerEventStation
import io.kevs.stream.addEventListener
import io.kevs.stream.impl.DefaultEventReceiveStream
import io.kevs.stream.impl.DefaultEventTransmitStream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventSerializationTest {

    @Event
    @EventIdentifier("test.event")
    private data class TestEvent(val someData: String)

    @Test
    fun `it should serialize and deserialize an event`() = runTest {
        val event = TestEvent("dummy")
        val dummyStation = SerializerEventStation.builder().build()
        val tx = DefaultEventTransmitStream.builder()
                .addDispatcher(dummyStation as EventDispatcher)
                .build()
        val rx = DefaultEventReceiveStream.builder()
                .addCollector(dummyStation as EventCollector)
                .build()

        rx.addEventListener<TestEvent> { assert(it.someData == event.someData) }
        tx.sendEvent(event)
    }


}
