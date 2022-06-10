package io.kevs.event.listener

fun interface EventListener<T : Any> {
    operator fun invoke(event: T)
}
