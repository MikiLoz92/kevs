package io.kevs.station.rabbitmq

fun interface ExchangeDiscriminator {
    /**
     * Given an event of a particular type, returns the exchange that should be used to publish it.
     */
    operator fun invoke(event: Any): String
}
