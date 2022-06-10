package io.kevs.ksp.options

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import io.kevs.ksp.options.KevsKspOptions.SerializationOptions.KotlinxSerializationOptions

data class KevsKspOptions(
    val serialization: SerializationOptions
) {
    class SerializationOptions(
        val kotlinx: KotlinxSerializationOptions
    ) {
        data class KotlinxSerializationOptions(
            val createSerializer: Boolean,
            val classDiscriminator: String?,
            val eventIdentifierAnnotation: String?
        )
    }

    companion object {
        fun SymbolProcessorEnvironment.parseKevsKspOptions(): KevsKspOptions {
            return KevsKspOptions(
                SerializationOptions(
                    KotlinxSerializationOptions(
                        options["io.kevs.serialization.kotlinx.create-serializer"]?.toBooleanStrict() ?: false,
                        options["io.kevs.serialization.kotlinx.class-discriminator"],
                        options["io.kevs.serialization.kotlinx.event-identifier-annotation"],
                    )
                )
            )
        }
    }
}
