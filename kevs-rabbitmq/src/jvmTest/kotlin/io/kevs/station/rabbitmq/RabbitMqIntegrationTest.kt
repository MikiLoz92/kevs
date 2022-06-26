package io.kevs.station.rabbitmq

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.rabbitmq.client.ConnectionFactory
import io.kevs.annotation.Event
import io.kevs.event.serialization.EventSerializerStub
import io.kevs.stream.addEventListener
import io.kevs.stream.impl.CoroutineEventReceiveStream
import io.kevs.stream.impl.DefaultEventTransmitStream
import io.kevs.stream.impl.sendEventBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.plus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.lang.IllegalArgumentException
import java.util.concurrent.Executors
import kotlin.test.Test

@Testcontainers
class RabbitMqIntegrationTest {

    @Container
    var rabbitmq: GenericContainer<*> =
        GenericContainer(DockerImageName.parse("rabbitmq:3.9.13-management-alpine"))
                .withExposedPorts(5672, 15672)
                .waitingFor(HttpWaitStrategy().forPort(15672))

    @Event
    @Serializable
    @SerialName("test.event")
    class TestEvent(val someData: String)

    @Test
    fun `it should run events listeners in different threads when using a thread pool executor`() {
        val event = TestEvent("something")

        val connection = ConnectionFactory().apply {
            username = "guest"
            password = "guest"
            host = "localhost"
            port = rabbitmq.getMappedPort(5672)
        }.newConnection()

        val channel = connection.createChannel()
        channel.exchangeDeclare(EXCHANGE_NAME, "topic")
        channel.queueDeclare(QUEUE_NAME, false, true, true, null)

        val coroutineScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher()) + SupervisorJob()
        val serializer = EventSerializerStub.kotlinxSerializationSimple(TestEvent.serializer())
        val tx = DefaultEventTransmitStream.builder()
                .addDispatcher(RabbitMqExchangeEventDispatcher(serializer, channel) {
                    RabbitMqEventPublishingSpecification("", QUEUE_NAME, false, null)
                })
                .build()
        val rx = CoroutineEventReceiveStream.builder()
                //.executor(Executors.newCachedThreadPool(ThreadFactoryBuilder().setNameFormat("listeners-%d").build()))
                .coroutineScope(CoroutineScope(Executors.newFixedThreadPool(10, ThreadFactoryBuilder().setNameFormat("listeners-coroutines-%d").build()).asCoroutineDispatcher() + SupervisorJob()))
                .addCollector(RabbitMqQueueEventCollector(QUEUE_NAME, serializer, channel, coroutineScope))
                .build()

        rx.addEventListener<TestEvent> {
            throw IllegalArgumentException()
            println(Thread.currentThread().name)
        }
        repeat(10000) { tx.sendEventBlocking(event) }

        //Thread.sleep(600_000)
    }

    companion object {
        private const val EXCHANGE_NAME = "events"
        private const val QUEUE_NAME = "example_queue"
    }
}
