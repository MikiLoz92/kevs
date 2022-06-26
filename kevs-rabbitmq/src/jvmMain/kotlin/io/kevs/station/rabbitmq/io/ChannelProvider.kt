package io.kevs.station.rabbitmq.io

import com.rabbitmq.client.Channel
import io.kevs.station.rabbitmq.subscriber.EventSubscriberSpecification

fun interface ChannelProvider {
    fun channel(eventSubscriberSpecification: EventSubscriberSpecification): Channel
}
