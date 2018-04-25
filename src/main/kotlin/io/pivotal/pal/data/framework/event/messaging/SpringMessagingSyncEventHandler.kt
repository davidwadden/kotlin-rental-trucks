package io.pivotal.pal.data.framework.event.messaging

import io.pivotal.pal.data.framework.event.SyncEventHandler
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder

class SpringMessagingSyncEventHandler<C, R>(private val eventName: String, private val channel: MessageChannel) : SyncEventHandler<C, R> {

    override fun onEvent(data: C): R? {
        val message = MessageBuilder.withPayload(data)
                .setHeader("eventName", eventName)
                .build()
        channel.send(message)

        return null
    }
}
