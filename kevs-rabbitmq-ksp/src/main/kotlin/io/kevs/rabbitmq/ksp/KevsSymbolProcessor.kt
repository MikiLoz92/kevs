package io.kevs.rabbitmq.ksp

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo
import io.kevs.annotation.Event
import io.kevs.rabbitmq.ksp.options.KevsKspRabbitMqOptions

class KevsSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: KevsKspRabbitMqOptions,
) : SymbolProcessor {

    var alreadyInvoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (alreadyInvoked) return emptyList()

        val symbols = resolver.getSymbolsWithAnnotation(Event::class.qualifiedName!!)
        buildKevsKotlinxSerializationEventSerializer(symbols, options)

        alreadyInvoked = true
        return symbols.filter { !it.validate() }.toList()
    }

    private fun buildKevsKotlinxSerializationEventSerializer(
        symbols: Sequence<KSAnnotated>,
        options: KevsKspRabbitMqOptions,
    ) {
        if (!options.eventCollectors.create) return

        FileSpec.builder("io.kevs.event.serialization", "KotlinxSerializationEventSerializer")
                .build()
                .writeTo(codeGenerator,
                         Dependencies(true, *symbols.map { it.containingFile!! }.toList().toTypedArray()))
    }

}
