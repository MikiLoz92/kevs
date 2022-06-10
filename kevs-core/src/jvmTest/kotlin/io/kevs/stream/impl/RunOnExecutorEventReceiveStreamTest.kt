package io.kevs.stream.impl

import io.kevs.annotation.Event
import io.kevs.event.serialization.EventSerializerStub
import io.kevs.station.EventCollector
import io.kevs.station.EventDispatcher
import io.kevs.station.impl.SerializerEventStation
import io.kevs.stream.addEventListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.awaitility.kotlin.await
import org.awaitility.kotlin.withPollInterval
import org.awaitility.pollinterval.FibonacciPollInterval
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RunOnExecutorEventReceiveStreamTest {

    @Event
    @Serializable
    @SerialName("test.event")
    class TestEvent(val someData: String)

    @Test
    fun `it should run events listeners in different threads when using a thread pool executor`() = runTest {
        val event = TestEvent("something")

        val serializer = EventSerializerStub.kotlinxSerializationSimple(TestEvent.serializer())
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

        val threads: MutableSet<String> = ConcurrentHashMap.newKeySet()

        rx.addEventListener<TestEvent> {
            Thread.sleep(1000)
            println(Thread.currentThread().name)
            threads.add(Thread.currentThread().name)
        }
        repeat(TIMES) { tx.sendEvent(event) }

        await.atMost(Duration.ofSeconds(2)).withPollInterval(FibonacciPollInterval()).until {
            threads.size == TIMES
        }
    }

    companion object {
        private const val TIMES = 1000
    }
}
