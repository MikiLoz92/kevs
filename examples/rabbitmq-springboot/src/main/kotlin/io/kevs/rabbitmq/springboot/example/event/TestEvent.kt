package io.kevs.rabbitmq.springboot.example.event

import io.kevs.annotation.EventIdentifier
import io.kevs.station.rabbitmq.annotation.ListenOnExchange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("example.test.event")
@EventIdentifier("example.test.event")
//@ListenOnExchange("some_domain_event_exchange")
//@ListenOnExchange("another_domain_event_exchange")
class TestEvent(val someData: String)
