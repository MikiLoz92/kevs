package io.kevs.stream.impl

import io.kevs.station.EventDispatcher
import kotlinx.coroutines.runBlocking

/**
 * Sends an event in a blocking manner by blocking the thread until all registered [EventDispatcher]s confirm that the
 * event has been dispatched.
 */
fun DefaultEventTransmitStream.sendEventBlocking(event: Any) = runBlocking { sendEvent(event) }
