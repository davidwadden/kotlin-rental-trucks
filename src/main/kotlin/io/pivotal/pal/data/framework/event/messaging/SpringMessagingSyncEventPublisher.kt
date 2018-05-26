package io.pivotal.pal.data.framework.event.messaging

import io.pivotal.pal.data.framework.event.DefaultSyncEventPublisher
import org.springframework.messaging.*
import org.springframework.messaging.support.MessageBuilder

class SpringMessagingSyncEventPublisher<C : Any, R : Any?>(
    eventName: String,
    input: SubscribableChannel,
    private val output: MessageChannel
) : DefaultSyncEventPublisher<C, R>(eventName), MessageHandler {

    init {
        input.subscribe(this)
    }

    @Throws(MessagingException::class)
    override fun handleMessage(message: Message<*>) {

        @Suppress("UNCHECKED_CAST")
        val data = message.payload as C
        val response = publish(data)

        if (response != null) {
            val outputMessage = MessageBuilder.withPayload(response)
                .setHeader("eventName", eventName)
                .build()
            output.send(outputMessage)
        }
    }
}
