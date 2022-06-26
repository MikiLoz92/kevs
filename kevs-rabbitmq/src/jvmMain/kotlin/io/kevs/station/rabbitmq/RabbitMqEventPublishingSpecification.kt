package io.kevs.station.rabbitmq

import com.rabbitmq.client.AMQP

data class RabbitMqEventPublishingSpecification(
    val exchange: String,
    val routingKey: String,
    val mandatory: Boolean,
    val properties: AMQP.BasicProperties?,
)
