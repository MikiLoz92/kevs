package io.kevs.event.serialization

interface EventSerializer {
    fun serialize(ev: Any): ByteArray
    fun deserialize(payload: ByteArray): Any
}
