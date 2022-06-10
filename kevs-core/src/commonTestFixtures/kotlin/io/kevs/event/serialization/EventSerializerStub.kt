package io.kevs.event.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

object EventSerializerStub {
    fun <T : Any> kotlinxSerializationSimple(serializer: KSerializer<T>): EventSerializer {
        return object : EventSerializer {
            override fun serialize(ev: Any): ByteArray = Json.encodeToString(serializer, ev as T).encodeToByteArray()
            override fun deserialize(payload: ByteArray): Any = Json.decodeFromString(serializer, payload.decodeToString())
        }
    }
}
