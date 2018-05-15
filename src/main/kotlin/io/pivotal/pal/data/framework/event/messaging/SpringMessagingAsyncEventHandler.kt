package io.pivotal.pal.data.framework.event.messaging

import io.pivotal.pal.data.framework.event.AsyncEventHandler
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder

class SpringMessagingAsyncEventHandler<T>(private val eventName: String, private val channel: MessageChannel) : AsyncEventHandler<T> {

    override fun onEvent(event: T) {
        val message = MessageBuilder.withPayload(event)
                .setHeader("eventName", eventName)
                .build()
        channel.send(message)
    }
}
