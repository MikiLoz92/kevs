package io.kevs.station.rabbitmq

fun interface RoutingKeyDiscriminator {
    /**
     * Given an event of a particular type, returns the routing key that should be used to publish it.
     */
    operator fun invoke(event: Any): String
}
