package io.kevs.station

import io.kevs.event.serialization.EventSerializer

interface SerializableEventCollector : EventCollector {

    val serializer: EventSerializer

}
