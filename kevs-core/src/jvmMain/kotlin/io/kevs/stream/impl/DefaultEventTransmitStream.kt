package io.kevs.stream.impl

import kotlinx.coroutines.runBlocking

fun DefaultEventTransmitStream.sendEventBlocking(event: Any) = runBlocking {
    dispatchers.onEach { it.dispatch(event) }
}
