package io.kevs.event.listener

fun interface EventListener<T : Any> {
    suspend operator fun invoke(event: T)
}
