package io.kevs.event.listener

import io.kevs.event.metadata.EventMetadata

interface ConditionalEventListener<T : Any> : EventListener<T> {
    fun shouldExecute(metadata: EventMetadata, event: T): Boolean
}
