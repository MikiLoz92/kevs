package io.kevs.stream.impl

import io.kevs.annotation.InternalKevsApi
import io.kevs.stream.EventReceiveStream
import java.util.concurrent.Executor

open class RunOnExecutorEventReceiveStream private constructor(
    private val defaultEventReceiveStream: DefaultEventReceiveStream,
    private val executor: Executor,
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
                    executor.execute { it.invoke(event) }
                }
    }

    companion object {
        fun builder() = Builder()
        open class Builder internal constructor() : DefaultEventReceiveStream.Companion.Builder<Builder, RunOnExecutorEventReceiveStream>() {
            private var executor: Executor? = null
            fun executor(executor: Executor) = apply { this.executor = executor }
            override fun build() = RunOnExecutorEventReceiveStream(
                super.build() as DefaultEventReceiveStream,
                executor ?: throw IllegalArgumentException("A RunOnExecutorEventReceiveStream needs an executor to run its event listeners on!")
            )
        }
    }
}
