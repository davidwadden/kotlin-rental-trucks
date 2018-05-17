package io.pivotal.pal.data.framework.event.kafka

import io.pivotal.pal.data.framework.event.AsyncEventHandler
import org.springframework.kafka.core.KafkaTemplate

class KafkaAsyncEventHandler<T>(private val eventName: String, private val template: KafkaTemplate<Any, T>) : AsyncEventHandler<T> {

    override fun onEvent(event: T) {
        template.send(eventName, event)
    }
}
