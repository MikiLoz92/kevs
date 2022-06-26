package io.kevs.rabbitmq.springboot.example

import io.kevs.rabbitmq.springboot.example.event.TestEvent
import io.kevs.station.rabbitmq.RabbitMqExchangeEventDispatcher
import io.kevs.stream.EventReceiveStream
import io.kevs.stream.EventTransmitStream
import io.kevs.stream.impl.DefaultEventTransmitStream
import kotlinx.coroutines.runBlocking
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

@Component
class StartupListener(
    //private val config: KevsRabbitMqConfiguration,
    private val eventReceiveStreams: List<EventReceiveStream>,
    private val appContext: ApplicationContext,
    private val eventTransmitStream: EventTransmitStream
) : ApplicationListener<ContextRefreshedEvent> {
    override fun onApplicationEvent(event: ContextRefreshedEvent) = runBlocking {
        repeat(1_000_000) { i -> eventTransmitStream.sendEvent(TestEvent("hey$i")) }
        //println(eventReceiveStreams)
    }
}
