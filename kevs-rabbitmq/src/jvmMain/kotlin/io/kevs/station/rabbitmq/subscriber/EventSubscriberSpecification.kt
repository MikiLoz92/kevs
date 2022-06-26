package io.kevs.station.rabbitmq.subscriber

import kotlin.reflect.KClass

/**
 * A class that is populated with all relevant information of an [EventSubscriber] in order for you to provide other
 * relevant elements that are required for Kevs to function.
 */
data class EventSubscriberSpecification(
    val clazz: KClass<out EventSubscriber<*>>,
    val consumerCount: Int,
)
