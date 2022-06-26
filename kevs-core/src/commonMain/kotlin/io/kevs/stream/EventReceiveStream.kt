package io.kevs.stream

import io.kevs.annotation.InternalKevsApi
import io.kevs.event.listener.ConditionalEventListener
import io.kevs.event.listener.EventListener
import io.kevs.event.listener.WithMetadataEventListener
import io.kevs.event.metadata.EventMetadata
import kotlin.reflect.KClass

interface EventReceiveStream : EventStream {
    fun <T : Any> addEventListener(eventClass: KClass<T>, listener: EventListener<T>)

    @InternalKevsApi
    suspend fun <T : Any> eventReceived(metadata: EventMetadata, event: T)

    @InternalKevsApi
    suspend fun <T : Any> executeListener(metadata: EventMetadata, event: T, listener: EventListener<T>) {
        if ((listener as? ConditionalEventListener<T>)?.shouldExecute(metadata, event) == false) return
        (listener as? WithMetadataEventListener<T>)?.invoke(metadata, event) ?: listener.invoke(event)
    }
}

inline fun <reified T : Any> EventReceiveStream.addEventListener(listener: EventListener<T>) {
    addEventListener(T::class, listener)
}
