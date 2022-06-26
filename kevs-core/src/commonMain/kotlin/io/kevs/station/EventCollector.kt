package io.kevs.station

import io.kevs.annotation.InternalKevsApi
import io.kevs.event.metadata.EventMetadata
import io.kevs.stream.EventReceiveStream

interface EventCollector {
    /**
     * A method that an [EventReceiveStream] will use to set up its callback function. You need to implement this
     * method and store the callback function somewhere, so you can call it later (when an event arrives) according to
     * your logic.
     */
    @InternalKevsApi
    fun defineCallback(callback: suspend (metadata: EventMetadata, event: Any) -> Unit)
}
