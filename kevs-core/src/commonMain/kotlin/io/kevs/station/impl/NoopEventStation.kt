package io.kevs.station.impl

import io.kevs.annotation.InternalKevsApi
import io.kevs.event.metadata.EventMetadata
import io.kevs.station.EventDispatcher
import io.kevs.station.EventCollector

/**
 * A no-op implementation of an event station. It gathers all events and routes them without serialization.
 */
class NoopEventStation : EventDispatcher, EventCollector {

    @InternalKevsApi
    override fun defineCallback(callback: suspend (metadata: EventMetadata, event: Any) -> Unit) {
        TODO("Not yet implemented")
    }

    override suspend fun dispatch(event: Any) {
        TODO("Not yet implemented")
    }
}
