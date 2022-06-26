package io.kevs.station.rabbitmq

import io.kevs.station.SerializableEventCollector
import java.util.concurrent.Executor

/**
 * An opinionated collector that treats messages as an individual execution unit. If processing goes OK (no exceptions
 * are thrown), the message will be ACK'd. If not, the message will be NACK'd.
 *
 * @param rabbitMqQueueEventCollector The collector that will be used to process messages. Its executor will be used to
 * run RabbitMQ consumers.
 * @param eolExecutor An Executor where messages that were processed (either OK or KO) will be sent to.
 * @param eolLambda The callback that will be executed when an event has been processed.
 */
class RabbitMqExecutionUnitQueueEventCollector(
    private val rabbitMqQueueEventCollector: RabbitMqQueueEventCollector,
    private val eolExecutor: Executor,
    private val eolLambda: (Any?, Throwable?) -> Unit,
) : SerializableEventCollector by rabbitMqQueueEventCollector
