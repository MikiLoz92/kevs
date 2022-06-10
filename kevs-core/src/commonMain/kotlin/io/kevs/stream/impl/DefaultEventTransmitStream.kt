package io.kevs.stream.impl

import io.kevs.annotation.InternalKevsApi
import io.kevs.station.EventDispatcher
import io.kevs.stream.EventTransmitStream
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.jvm.JvmStatic

/**
 * Default, blocking implementation of an [EventTransmitStream]. The [sendEvent] method blocks until all registered
 * listeners have been executed.
 */
@OptIn(InternalKevsApi::class)
open class DefaultEventTransmitStream private constructor(
    @property:InternalKevsApi val dispatchers: List<EventDispatcher>
) : EventTransmitStream {

    override suspend fun sendEvent(event: Any) {
        dispatchers.onEach { it.dispatch(event) }
    }

    companion object {
        @JvmStatic
        fun builder() = Builder<Builder<*>>()
        open class Builder<B> internal constructor() {
            private val dispatchers: MutableSet<EventDispatcher> = mutableSetOf()

            fun addDispatcher(dispatcher: EventDispatcher): Builder<B> = apply { dispatchers.add(dispatcher) }
            fun build() = DefaultEventTransmitStream(dispatchers.toList())
        }
    }
}
