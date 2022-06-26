package io.kevs.station.rabbitmq.configuration

import com.rabbitmq.client.Channel
import io.kevs.station.rabbitmq.RabbitMqQueueEventCollector

/**
 * Initializes the RabbitMQ infrastructure after all Kevs-related RabbitMQ components have been declared and loaded
 * into the classpath.
 */
class KevsRabbitMqConfigurer {

    fun initialize(
        channel: Channel,
        bindings: Iterable<RabbitMqFanoutExchangeBinding>,
        collectors: Iterable<RabbitMqQueueEventCollector>,
    ) {
        bindings.map { it.queue }.toSet().onEach { channel.queueDeclare(it, true, false, false, emptyMap()) }
        bindings.map { it.exchange }.toSet().onEach { channel.exchangeDeclare(it, "fanout", true, false, emptyMap()) }
        for (b in bindings) channel.queueBind(b.queue, b.exchange, b.routingKey)
        collectors.onEach { it.initialize() }
    }

    data class RabbitMqFanoutExchangeBinding(val queue: String, val exchange: String, val routingKey: String)

}
