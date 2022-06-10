package io.kevs.station

interface EventDispatcher : EventStation {
    suspend fun dispatch(event: Any)
}
