package io.kevs.stream.impl

import io.kevs.annotation.InternalKevsApi
import io.kevs.event.listener.EventListener
import io.kevs.event.metadata.EventMetadata
import io.kevs.station.EventCollector
import io.kevs.stream.EventReceiveStream
import kotlin.reflect.KClass

@OptIn(InternalKevsApi::class)
open class DefaultEventReceiveStream private constructor(
    @property:InternalKevsApi val collectors: List<EventCollector>
) : EventReceiveStream {

    @InternalKevsApi val listeners: MutableMap<KClass<out Any>, MutableList<EventListener<in Any>>> = mutableMapOf()

    init {
        collectors.onEach { collector ->
            collector.defineCallback(::eventReceived)
        }
    }

    override fun <T : Any> addEventListener(eventClass: KClass<T>, listener: EventListener<T>) {
        listeners.getOrPut(eventClass) { mutableListOf() }.add(listener as EventListener<Any>)
    }

    @InternalKevsApi
    override suspend fun <T : Any> eventReceived(metadata: EventMetadata, event: T) {
        listeners
                .filter { it.key == event::class }
                .flatMap { it.value }
                .onEach { executeListener(metadata, event, it) }
    }

    companion object {
        fun builder() = Builder<Builder<*, DefaultEventReceiveStream>, DefaultEventReceiveStream>()
        open class Builder<B, RS> @InternalKevsApi constructor() {
            private val collectors: MutableList<EventCollector> = mutableListOf()

            open fun addCollector(collector: EventCollector): Builder<B, RS> = apply { collectors.add(collector) }
            open fun build(): RS = DefaultEventReceiveStream(collectors) as RS
        }
    }
}
