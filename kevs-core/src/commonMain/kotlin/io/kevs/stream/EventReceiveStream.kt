package io.kevs.stream

import io.kevs.annotation.InternalKevsApi
import io.kevs.event.listener.EventListener
import kotlin.reflect.KClass

interface EventReceiveStream : EventStream {
    fun <T : Any> addEventListener(eventClass: KClass<T>, listener: EventListener<T>)

    @InternalKevsApi
    suspend fun <T : Any> eventReceived(event: T)
}

inline fun <reified T : Any> EventReceiveStream.addEventListener(listener: EventListener<T>) {
    addEventListener(T::class, listener)
}
