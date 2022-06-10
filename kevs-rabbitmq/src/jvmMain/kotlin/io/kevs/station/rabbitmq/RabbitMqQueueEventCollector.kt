package io.kevs.station.rabbitmq

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Connection
import com.rabbitmq.client.Consumer
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import io.kevs.annotation.InternalKevsApi
import io.kevs.event.serialization.EventSerializer
import io.kevs.station.SerializableEventCollector
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class RabbitMqQueueEventCollector(
    override val serializer: EventSerializer,
    private val connection: Connection,
    private val queue: String,
    private val executor: Executor,
) : SerializableEventCollector {

    private val channel = connection.createChannel()
    private val channelPublishExecutor = Executors.newSingleThreadExecutor(
        ThreadFactoryBuilder().setNameFormat("channel-publish-%d").build()
    )

    private lateinit var callback: suspend ((event: Any) -> Unit)
    private val consumer: Consumer = object : DefaultConsumer(channel) {
        override fun handleDelivery(consumerTag: String,
                                    envelope: Envelope,
                                    properties: AMQP.BasicProperties?,
                                    body: ByteArray?) {
            if (body == null) return

            rabbitMqConsumerCoroutineContext.launch {
                val event = serializer.deserialize(body)
                val successful = try {
                    callback(event)
                    true
                } catch (t: Throwable) {
                    false
                }

                println(Thread.currentThread().name)
                delay(10000)

                channelPublishExecutor.execute {
                    println(Thread.currentThread().name)

                    if (successful)
                        channel.basicAck(envelope.deliveryTag, false)
                    else
                        channel.basicNack(envelope.deliveryTag, false, false)
                }
            }
        }
    }

    init {
        channel.basicConsume(queue, consumer)
    }

    @InternalKevsApi
    override fun defineCallback(callback: suspend (event: Any) -> Unit) {
        this.callback = callback
    }

}
