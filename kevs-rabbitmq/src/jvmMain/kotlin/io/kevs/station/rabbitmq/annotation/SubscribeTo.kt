package io.kevs.station.rabbitmq.annotation

import kotlin.reflect.KClass

annotation class SubscribeTo(val event: KClass<*>)
