package io.kevs.rabbitmq.ksp.options

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment

data class KevsKspRabbitMqOptions(
    val eventSubscribers: EventSubscriberOptions,
    val eventCollectors: CollectorOptions,
) {

    class EventSubscriberOptions(
        val create: Boolean
    )

    class CollectorOptions(
        val create: Boolean
    )

    companion object {
        fun SymbolProcessorEnvironment.parseKevsKspOptions(): KevsKspRabbitMqOptions {
            return KevsKspRabbitMqOptions(
                EventSubscriberOptions(
                    options["io.kevs.rabbitmq.event-subscribers.create"]?.toBooleanStrict() ?: false,
                ),
                CollectorOptions(
                    options["io.kevs.rabbitmq.event-collectors.create"]?.toBooleanStrict() ?: false,
                )
            )
        }
    }
}
