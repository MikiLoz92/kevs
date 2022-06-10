package io.kevs.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import io.kevs.ksp.options.KevsKspOptions.Companion.parseKevsKspOptions

class KevsProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return KevsSymbolProcessor(environment.codeGenerator, environment.logger, environment.parseKevsKspOptions())
    }
}
