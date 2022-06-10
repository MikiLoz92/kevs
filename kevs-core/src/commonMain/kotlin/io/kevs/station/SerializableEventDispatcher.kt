package io.kevs.station

import io.kevs.event.serialization.EventSerializer

interface SerializableEventDispatcher : EventDispatcher {
    val serializer: EventSerializer
}
