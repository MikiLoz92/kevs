@file:OptIn(DelicateCoroutinesApi::class)

package io.kevs.station.rabbitmq

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Consumer
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import io.kevs.annotation.InternalKevsApi
import io.kevs.event.metadata.EventMetadata
import io.kevs.event.metadata.EventWithMetadata
import io.kevs.event.serialization.EventSerializer
import io.kevs.station.SerializableEventCollector
import io.kevs.station.rabbitmq.metrics.MetricsCollector
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.Executor
import kotlin.time.Duration.Companion.nanoseconds

/**
 * A collector that collects events from a RabbitMQ queue.
 * @param serializer The serializer to use when deserializing events.
 * @param channel The RabbitMQ channel where you want Kevs' RabbitMQ consumers to execute on.
 * @param connection The RabbitMQ connection to use. If a channel is not defined, a standalone one will be created
 * using this connection, that will just be used to execute RabbitMQ consumers.
 * @param queue The RabbitMQ queue to listen for events from.
 * @param executor When consumers are executed, this executor will be used to submit the subscribers' logic. In other
 * words, the processing of the event will be done through this consumer. Take into account any possible side-effects
 * this might have in terms of the multi-threading capabilities of the RabbitMQ Java client (ACKs are thread-safe, but
 * publishing is not!). If no executor is provided, a default one will be used. The default executor operates on a
 * single thread to avoid race conditions.
 * @param consumerExecutionCoroutineScope A [CoroutineScope] where the [Consumer] logic is executed on. If you don't
 * provide one, the logic will be executed by blocking the Consumer thread (even though the [callback] is a suspending
 * function, it will be wrapped with a [runBlocking] call).
 * @param rabbitMqOperationsCoroutineScope A [CoroutineScope] where all RabbitMQ operations on this channel will be
 * performed on. Because a Channel is not thread safe for most operations, you should consider using a single-threaded
 * [Executor] converted into a [CoroutineDispatcher], by applying the [asCoroutineDispatcher] method on the Executor.
 */
open class RabbitMqQueueEventCollector(
    val queue: String,
    override val serializer: EventSerializer,
    private val channel: Channel,
    private val consumerExecutionCoroutineScope: CoroutineScope?,
    private val metricsCollector: MetricsCollector? = null
) : SerializableEventCollector {

    private lateinit var callback: suspend ((metadata: EventMetadata, event: Any) -> Unit)
    private val consumer: Consumer = object : DefaultConsumer(channel) {
        override fun handleDelivery(consumerTag: String,
                                    envelope: Envelope,
                                    properties: AMQP.BasicProperties?,
                                    body: ByteArray?) {
            val startTime = System.nanoTime()
            if (body == null) return

            val block: suspend CoroutineScope.() -> Unit = {
                val event = serializer.deserialize(body)
                val successful = try {
                    if (event is EventWithMetadata) callback(event.metadata, event.event)
                    else callback(EventMetadata(), event)
                    true
                } catch (t: Throwable) {
                    false
                }

                /*println(Thread.currentThread().name)
                delay(10000)*/

                mutexes.getOrPut(channel) { Mutex() }.withLock {
                    //println(Thread.currentThread().name)
                    if (successful)
                        this@RabbitMqQueueEventCollector.channel.basicAck(envelope.deliveryTag, false)
                    else
                        this@RabbitMqQueueEventCollector.channel.basicNack(envelope.deliveryTag, false, false)
                }

                metricsCollector?.eventListenerExecuted(event)
                metricsCollector?.eventListenerExecutionDuration(event, (System.nanoTime() - startTime).nanoseconds)
            }

            consumerExecutionCoroutineScope?.launch(block = block) ?: runBlocking(block = block)
        }
    }

    fun initialize() {
        runBlocking(Dispatchers.IO) {
            mutexes.getOrPut(channel) { Mutex() }.withLock {
            //mutex.withLock {
                channel.basicConsume(queue, consumer)
            }
        }
    }

    @InternalKevsApi
    override fun defineCallback(callback: suspend (metadata: EventMetadata, event: Any) -> Unit) {
        this.callback = callback
    }

    companion object {
        // TODO: Create a map from Channel instance to Mutex, so as to not block all threads, but just the ones that
        //  are using a particular channel.
        val mutexes = mutableMapOf<Channel, Mutex>()
        val mutex: Mutex = Mutex()
    }

}
