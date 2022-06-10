package io.kevs.event.metadata

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
data class EventWithMetadata(val metadata: EventMetadata, @Polymorphic val event: Any)
