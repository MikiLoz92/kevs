package io.kevs.station.rabbitmq.io

import io.kevs.station.rabbitmq.subscriber.EventSubscriberSpecification
import kotlinx.coroutines.CoroutineScope

fun interface CoroutineScopeProvider {
    fun coroutineScope(eventSubscriberSpecification: EventSubscriberSpecification): CoroutineScope
}
