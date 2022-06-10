package io.kevs.ksp

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.Import
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import io.kevs.event.metadata.EventMetadata
import io.kevs.event.metadata.EventWithMetadata
import io.kevs.event.serialization.EventSerializer
import io.kevs.ksp.options.KevsKspOptions
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.intellij.lang.annotations.Language

class KevsSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: KevsKspOptions,
) : SymbolProcessor {

    var alreadyInvoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (alreadyInvoked) return emptyList()

        val symbols = resolver.getSymbolsWithAnnotation("io.kevs.annotation.Event")
        buildKevsKotlinxSerializationEventSerializer(symbols, options)

        /*logger.info(resolver.getAllFiles().joinToString())
        logger.info(symbols.joinToString())
        (resolver.getAllFiles()
                .first().declarations.first() as KSClassDeclaration).annotations.let { println(it.joinToString()) }
        val ret = symbols.filter { !it.validate() }.toList()
        symbols
                .filter { it is KSClassDeclaration && it.validate() }
                .forEach {
                    it.accept(EventsVisitor(), Unit)
                }
        return ret*/

        alreadyInvoked = true
        return symbols.filter { !it.validate() }.toList()
    }

    private fun buildKevsKotlinxSerializationEventSerializer(
        symbols: Sequence<KSAnnotated>,
        options: KevsKspOptions,
    ) {
        if (!options.serialization.kotlinx.createSerializer) return

        FileSpec.builder("io.kevs.event.serialization", "KotlinxSerializationEventSerializer")
                .addImport("kotlinx.serialization.modules", "plus")
                .addType(
                    TypeSpec.classBuilder("KotlinxSerializationEventSerializer")
                            .primaryConstructor(
                                FunSpec.constructorBuilder()
                                        .addParameter("json", Json::class)
                                        .build()
                            )
                            .addSuperinterface(EventSerializer::class)
                            .addProperty(
                                PropertySpec.builder("json", Json::class)
                                        .initializer(
                                            CodeBlock.builder()
                                                    .beginControlFlow("%T(json)", Json::class)
                                                    .beginControlFlow("serializersModule += %M",
                                                                      MemberName("kotlinx.serialization.modules",
                                                                                 "SerializersModule"))
                                                    .beginControlFlow("%M(%T::class)",
                                                                      MemberName("kotlinx.serialization.modules",
                                                                                 "polymorphic"),
                                                                      Any::class)
                                                    .apply {
                                                        for (s in symbols) {
                                                            this.addStatement(
                                                                "%M(%T::class)",
                                                                MemberName("kotlinx.serialization.modules",
                                                                           "subclass"),
                                                                (s as? KSClassDeclaration)?.toClassName()
                                                            )
                                                        }
                                                    }
                                                    .endControlFlow()
                                                    .endControlFlow()
                                                    .endControlFlow()
                                                    .build()
                                        )
                                        .addModifiers(KModifier.PRIVATE)
                                        .build()
                            )
                            .addFunction(
                                FunSpec.builder("serialize")
                                        .addModifiers(KModifier.OVERRIDE)
                                        .addParameter("ev", Any::class)
                                        .returns(ByteArray::class)
                                        .addCode(
                                            CodeBlock.builder()
                                                    .addStatement("return json.%M(%T(%T(), ev)).encodeToByteArray()",
                                                                  MemberName("kotlinx.serialization",
                                                                             "encodeToString",
                                                                             true),
                                                                  EventWithMetadata::class,
                                                                  EventMetadata::class)
                                                    .build()
                                        )
                                        .build()
                            )
                            .addFunction(
                                FunSpec.builder("deserialize")
                                        .addModifiers(KModifier.OVERRIDE)
                                        .addParameter("payload", ByteArray::class)
                                        .returns(Any::class)
                                        .addCode(
                                            CodeBlock.builder()
                                                    .addStatement("""
                                                        return json.decodeFromString(%T.serializer(), payload.decodeToString()).event
                                                    """.trimIndent(), EventWithMetadata::class)
                                                    .build()
                                        )
                                        .build()
                            )
                            .build()
                )
                .build()
                .writeTo(codeGenerator,
                         Dependencies(true, *symbols.map { it.containingFile!! }.toList().toTypedArray()))
    }

    inner class EventsVisitor : KSVisitorVoid() {

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            classDeclaration.primaryConstructor!!.accept(this, data)
        }

        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            val parent = function.parentDeclaration as KSClassDeclaration
            val packageName = parent.containingFile!!.packageName.asString()
            val className = parent.simpleName.asString()
            //val file = codeGenerator.createNewFile(Dependencies(true, function.containingFile!!), packageName, className)
            FileSpec.builder(packageName, className).build()
                    .writeTo(codeGenerator, Dependencies(true, function.containingFile!!))
        }
    }

}
