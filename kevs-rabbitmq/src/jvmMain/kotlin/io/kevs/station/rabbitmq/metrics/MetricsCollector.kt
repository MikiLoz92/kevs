package io.kevs.station.rabbitmq.metrics

import io.kevs.station.rabbitmq.RabbitMqEventPublishingSpecification
import kotlin.time.Duration

interface MetricsCollector {
    fun eventDispatched(event: Any, spec: RabbitMqEventPublishingSpecification)
    fun eventDispatchError(event: Any, spec: RabbitMqEventPublishingSpecification?, throwable: Throwable)
    fun eventCollected(event: Any)
    fun eventCollectionError(event: Any, throwable: Throwable)
    fun eventListenerExecuted(event: Any)
    fun eventListenerExecutionDuration(event: Any, duration: Duration)
}
