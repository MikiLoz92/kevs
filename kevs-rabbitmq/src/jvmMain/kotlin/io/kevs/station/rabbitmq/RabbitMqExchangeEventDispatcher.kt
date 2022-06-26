package io.kevs.station.rabbitmq

import com.rabbitmq.client.Channel
import io.kevs.event.serialization.EventSerializer
import io.kevs.station.SerializableEventDispatcher
import io.kevs.station.rabbitmq.metrics.MetricsCollector
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet

/**
 * @constructor Builds a RabbitMQ dispatcher that uses the provided [publishSpec] lambda to provide the actual RabbitMQ
 * publish specification, depending on the particular event. This lambda will be called each time a new event is
 * request for publication, in order to discern how it should be published (which exchange, routing key, properties,
 * etc.).
 */
class RabbitMqExchangeEventDispatcher(
    override val serializer: EventSerializer,
    private val channel: Channel,
    private val metricsCollector: MetricsCollector? = null,
    private val publishSpec: ((event: Any) -> RabbitMqEventPublishingSpecification)
) : SerializableEventDispatcher {

    override suspend fun dispatch(event: Any) {
        val spec = try {
            publishSpec(event)
        } catch (t: Throwable) {
            metricsCollector?.eventDispatchError(event, null, t)
            return
        }
        try {
            mutex.withLock {
                with(spec) {
                    channel.basicPublish(exchange, routingKey, mandatory, properties, serializer.serialize(event))
                }
                metricsCollector?.eventDispatched(event, spec)
            }
        } catch (t: Throwable) {
            metricsCollector?.eventDispatchError(event, spec, t)
        }
    }

    companion object {
        // TODO: Create a map from Channel instance to Mutex, so as to not block all threads, but just the ones that
        //  are using a particular channel.
        val mutex: Mutex = Mutex()
    }

}
