package io.kevs.rabbitmq.springboot.example.configuration

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import io.kevs.event.serialization.EventSerializer
import io.kevs.event.serialization.KotlinxSerializationEventSerializer
import io.kevs.rabbitmq.binding.EventBindingRegistrar
import io.kevs.station.rabbitmq.RabbitMqEventPublishingSpecification
import io.kevs.station.rabbitmq.RabbitMqExchangeEventDispatcher
import io.kevs.station.rabbitmq.RabbitMqQueueEventCollector
import io.kevs.station.rabbitmq.configuration.KevsRabbitMqConfiguration
import io.kevs.station.rabbitmq.configuration.KevsRabbitMqConfigurer
import io.kevs.station.rabbitmq.io.ChannelProvider
import io.kevs.station.rabbitmq.io.CoroutineScopeProvider
import io.kevs.stream.EventTransmitStream
import io.kevs.stream.impl.DefaultEventTransmitStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors

@Configuration
class KevsConfiguration {

    @Bean
    fun json() = Json { ignoreUnknownKeys = true }

    @Bean
    fun rabbitMqConnection(): Connection = ConnectionFactory().apply {
        host = "localhost"
        port = 5672
        username = "guest"
        password = "guest"
        virtualHost = "/"
    }.newConnection()

    @Bean
    fun rabbitMqInitializationChannel(connection: Connection): Channel = connection.createChannel()

    @Bean
    fun defaultEventSubscribersChannel(connection: Connection): Channel = connection.createChannel().apply { basicQos(50, false) }

    @Bean
    fun defaultEventDispatchChannel(connection: Connection): Channel = connection.createChannel()

    @Bean(name = ["channelProvider"])
    fun channelProvider(@Qualifier("defaultEventSubscribersChannel") channel: Channel) = ChannelProvider { channel }

    @Bean(name = ["coroutineScopeProvider"])
    fun coroutineScopeProvider() = CoroutineScopeProvider {
        CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher() + SupervisorJob())
    }

    @Bean
    fun defaultEventSerializer(json: Json) = KotlinxSerializationEventSerializer(json)

    @Bean
    fun kevsRabbitMqConfiguration(json: Json, channelProvider: ChannelProvider) = KevsRabbitMqConfiguration.builder()
            .setDefaultEventPublishingExchangeName(DOMAIN_EVENTS_EXCHANGE_NAME)
            .setDefaultEventSubscriberConsumerCount(4)
            .setDefaultEventSerializer(KotlinxSerializationEventSerializer(json))
            .setDefaultChannelProvider(channelProvider)
            .build()

    @Bean
    fun eventTransmitStream(
        defaultEventSerializer: EventSerializer,
        @Qualifier("defaultEventDispatchChannel") channel: Channel,
    ): EventTransmitStream {
        val dispatcher = RabbitMqExchangeEventDispatcher(defaultEventSerializer, channel) { event: Any ->
            RabbitMqEventPublishingSpecification(DOMAIN_EVENTS_EXCHANGE_NAME, "example.test.event", true, null)
        }
        return DefaultEventTransmitStream.builder()
                .addDispatcher(dispatcher)
                .build()
    }

    @Bean
    fun kevsRabbitMqInitializer(
        @Qualifier("rabbitMqInitializationChannel") channel: Channel,
        eventBindingRegistrar: EventBindingRegistrar,
        collectors: List<RabbitMqQueueEventCollector>
    ) = KevsRabbitMqConfigurer().apply {
        initialize(channel, eventBindingRegistrar.bindings, collectors)
        channel.close()
    }

    companion object {
        private const val DOMAIN_EVENTS_EXCHANGE_NAME = "domain_events"
    }
}
