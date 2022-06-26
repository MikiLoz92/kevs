package io.kevs.station.rabbitmq.subscriber

fun interface EventSubscriber<T : Any> {
    suspend operator fun invoke(event: T)
}
