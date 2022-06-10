package io.kevs.stream.impl

import io.kevs.annotation.InternalKevsApi
import io.kevs.stream.EventReceiveStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

@OptIn(InternalKevsApi::class)
open class CoroutineEventReceiveStream private constructor(
    private val defaultEventReceiveStream: DefaultEventReceiveStream,
    private val coroutineScope: CoroutineScope,
) : EventReceiveStream by defaultEventReceiveStream {

    init {
        defaultEventReceiveStream.collectors.onEach { collector ->
            collector.defineCallback(::eventReceived)
        }
    }

    @OptIn(InternalKevsApi::class)
    override suspend fun <T : Any> eventReceived(event: T) {
        defaultEventReceiveStream.listeners
                .filter { it.key == event::class }
                .flatMap { it.value }
                .onEach {
                    withContext(coroutineScope.coroutineContext) { it.invoke(event) }
                }
    }

    companion object {
        @JvmStatic
        fun builder() = Builder()
        open class Builder @InternalKevsApi internal constructor() : DefaultEventReceiveStream.Companion.Builder<Builder, CoroutineEventReceiveStream>() {
            private var coroutineScope: CoroutineScope? = null
            fun coroutineScope(coroutineScope: CoroutineScope) = apply { this.coroutineScope = coroutineScope }
            override fun build() = CoroutineEventReceiveStream(
                super.build() as DefaultEventReceiveStream,
                coroutineScope ?: throw IllegalArgumentException("A CoroutineEventReceiveStream needs a CoroutineScope to run its event listeners on!")
            )
        }
    }
}
