package io.kevs.example

import io.kevs.annotation.Event
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("example.test.event")
@Event
class TestEvent(val someData: String)
