package io.pivotal.pal.data.framework.event.messaging

import io.pivotal.pal.data.framework.event.DefaultAsyncEventPublisher
import org.springframework.messaging.Message
import org.springframework.messaging.MessageHandler
import org.springframework.messaging.MessagingException
import org.springframework.messaging.SubscribableChannel

class SpringMessagingAsyncEventPublisher<T>(eventName: String, channel: SubscribableChannel) : DefaultAsyncEventPublisher<T>(eventName), MessageHandler {

    init {
        channel.subscribe(this)
    }

    @Throws(MessagingException::class)
    override fun handleMessage(message: Message<*>) {

        @Suppress("UNCHECKED_CAST")
        val data = message.payload as T
        publish(data)
    }
}
