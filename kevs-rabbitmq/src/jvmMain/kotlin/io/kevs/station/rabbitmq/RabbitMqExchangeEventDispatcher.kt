package io.kevs.station.rabbitmq

import com.rabbitmq.client.Channel
import io.kevs.event.serialization.EventSerializer
import io.kevs.station.SerializableEventDispatcher

class RabbitMqExchangeEventDispatcher(
    override val serializer: EventSerializer,
    private val channel: Channel,
    private val publisher: ((event: Any) -> RabbitMqEventPublishingSpecification),
    /*private val exchangeDiscriminator: ExchangeDiscriminator,
    private val routingKeyDiscriminator: RoutingKeyDiscriminator,*/
) : SerializableEventDispatcher {

    override suspend fun dispatch(event: Any) {
        val spec = publisher(event)
        channel.basicPublish(spec.exchange, spec.routingKey, true, spec.properties, serializer.serialize(event))
    }

}
