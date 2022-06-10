package io.kevs.station.impl

import io.kevs.annotation.InternalKevsApi
import io.kevs.event.serialization.EventSerializer
import io.kevs.station.EventCollector
import io.kevs.station.EventDispatcher
import io.kevs.station.EventStation

/**
 * A simple [EventStation] implementation that serializes and deserializes back the events when routing them.
 */
@OptIn(InternalKevsApi::class)
class SerializerEventStation private constructor(
    private val serializer: EventSerializer
): EventDispatcher, EventCollector, EventSerializer by serializer {

    //private val eventQueue: Queue<Any> = LinkedList()
    private lateinit var callback: suspend (event: Any) -> Unit

    override suspend fun dispatch(event: Any) {
        val serializedEvent = serializer.serialize(event)
        println(serializedEvent.decodeToString())
        val deserializedEvent = serializer.deserialize(serializedEvent)
        callback.invoke(deserializedEvent)
    }

    override fun defineCallback(callback: suspend (event: Any) -> Unit) {
        this.callback = callback
    }

    companion object {
        fun builder() = Builder<Builder<*>>()
        open class Builder<T> internal constructor() {
            private var serializer: EventSerializer? = null

            fun serializer(serializer: EventSerializer) = apply { this.serializer = serializer }
            fun build() = SerializerEventStation(
                serializer ?: throw IllegalArgumentException("You need to provide a serializer!"),
            )
        }
    }

}
