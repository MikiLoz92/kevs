package io.kevs.station

import io.kevs.annotation.InternalKevsApi
import io.kevs.stream.EventTransmitStream

interface EventCollector {
    /**
     * A method that an [EventTransmitStream] will use to set up its callback function. You need to implement this
     * method and store the callback function somewhere, so you can call it later, when an event arrives according to
     * your logic.
     */
    @InternalKevsApi
    fun defineCallback(callback: suspend (event: Any) -> Unit)
}
