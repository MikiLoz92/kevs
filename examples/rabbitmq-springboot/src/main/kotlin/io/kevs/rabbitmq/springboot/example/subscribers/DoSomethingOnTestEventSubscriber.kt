package io.kevs.rabbitmq.springboot.example.subscribers

import io.kevs.rabbitmq.springboot.example.event.TestEvent
import io.kevs.station.rabbitmq.annotation.ConsumerCount
import io.kevs.station.rabbitmq.annotation.SubscribeTo
import io.kevs.station.rabbitmq.annotation.spring.WithChannelProvider
import io.kevs.station.rabbitmq.annotation.spring.WithCoroutineScopeProvider
import io.kevs.station.rabbitmq.subscriber.EventSubscriber
import org.springframework.stereotype.Component

@Component
@WithChannelProvider("channelProvider")
@WithCoroutineScopeProvider("coroutineScopeProvider")
@SubscribeTo(TestEvent::class)
@ConsumerCount(16)
class DoSomethingOnTestEventSubscriber : EventSubscriber<TestEvent> {
    override suspend fun invoke(event: TestEvent) {
        //println("Event ${event.someData} executing on thread ${Thread.currentThread().name}")
    }
}
