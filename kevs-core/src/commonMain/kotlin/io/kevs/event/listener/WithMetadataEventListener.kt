package io.kevs.event.listener

import io.kevs.event.metadata.EventMetadata

fun interface WithMetadataEventListener<T : Any> : EventListener<T> {
    override suspend fun invoke(event: T) { /* don't do anything, we don't need it */ }
    suspend operator fun invoke(metadata: EventMetadata, event: T)
}
