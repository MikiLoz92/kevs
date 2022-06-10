package io.kevs.station.rabbitmq

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.rabbitmq.client.Consumer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

/**
 * [CoroutineScope] that manages all [Consumer] dispatches.
 */
val rabbitMqConsumerCoroutineContext = CoroutineScope(
    Executors.newCachedThreadPool(
        ThreadFactoryBuilder().setNameFormat("kevs-rabbitmq-collectors-%d").build()
    ).asCoroutineDispatcher() + SupervisorJob()
)
