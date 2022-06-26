package io.kevs.station.rabbitmq.configuration

import io.kevs.event.serialization.EventSerializer
import io.kevs.station.rabbitmq.io.ChannelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import net.pearx.kasechange.CaseFormat
import net.pearx.kasechange.toSnakeCase
import java.util.concurrent.Executors

class KevsRabbitMqConfiguration internal constructor(
    val rabbitMqQueueNameGenerator: (String) -> String,
    val defaultEventPublishingExchangeName: String,
    val defaultEventSubscriberConsumerCount: Int,
    val defaultChannelProvider: ChannelProvider,
    val defaultEventSerializer: EventSerializer,
    val defaultConsumerExecutionCoroutineScope: CoroutineScope,
) {

    companion object {
        @JvmStatic
        fun builder() = Builder()

        class Builder internal constructor() {

            private var rabbitMqQueueNameGenerator: (String) -> String =
                { fqdn -> fqdn.split(".").last().toSnakeCase(CaseFormat.CAPITALIZED_CAMEL) }
            fun setRabbitMqQueueNameGenerator(generator: (eventSubscriberFqdn: String) -> String) =
                apply { this.rabbitMqQueueNameGenerator = generator }

            private var defaultEventPublishingExchangeName: String = "kevs_events"
            fun setDefaultEventPublishingExchangeName(exchangeName: String) =
                apply { this.defaultEventPublishingExchangeName = exchangeName }

            private var defaultEventSubscriberConsumerCount: Int = 1
            fun setDefaultEventSubscriberConsumerCount(count: Int) =
                apply { this.defaultEventSubscriberConsumerCount = count }

            private var defaultChannelProvider: ChannelProvider? = null
            fun setDefaultChannelProvider(provider: ChannelProvider) =
                apply { this.defaultChannelProvider = provider }

            private var defaultEventSerializer: EventSerializer? = null
            fun setDefaultEventSerializer(serializer: EventSerializer) =
                apply { this.defaultEventSerializer = serializer }

            private var defaultConsumerExecutionCoroutineScope: CoroutineScope =
                CoroutineScope(Executors.newCachedThreadPool().asCoroutineDispatcher() + SupervisorJob())
            fun setDefaultConsumerExecutionCoroutineScope(coroutineScope: CoroutineScope) =
                apply { this.defaultConsumerExecutionCoroutineScope = coroutineScope }

            fun build() = KevsRabbitMqConfiguration(
                rabbitMqQueueNameGenerator,
                defaultEventPublishingExchangeName,
                defaultEventSubscriberConsumerCount,
                defaultChannelProvider ?: throw IllegalArgumentException("You need to provide a default RabbitMQ channel provider!"),
                defaultEventSerializer ?: throw IllegalArgumentException("You need to provide an event serializer!"),
                defaultConsumerExecutionCoroutineScope
            )
        }
    }
}
