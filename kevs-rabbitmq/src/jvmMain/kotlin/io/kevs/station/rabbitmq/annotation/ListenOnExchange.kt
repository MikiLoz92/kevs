package io.kevs.station.rabbitmq.annotation

@Repeatable
annotation class ListenOnExchange(val exchangeName: String)
