package io.kevs.stream

import io.kevs.station.EventDispatcher
import io.kevs.stream.impl.DefaultEventTransmitStream

interface EventTransmitStream : EventStream {

    /**
     * Sends an event. This function blocks until all registered [EventDispatcher]s to confirm that the event has been
     * sent.
     */
    //fun sendEventBlocking(event: Any)

    /**
     * Sends an event. This function blocks until all registered [EventDispatcher]s to confirm that the event has been
     * sent.
     */
    suspend fun sendEvent(event: Any)

    /**
     * Sends an event. This function does not wait for the dispatch confirmation from the registered [EventDispatcher]s.
     */
    //fun fireEvent(event: Any)

}
