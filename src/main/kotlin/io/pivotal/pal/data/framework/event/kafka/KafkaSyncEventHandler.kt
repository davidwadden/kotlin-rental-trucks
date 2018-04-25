package io.pivotal.pal.data.framework.event.kafka

import io.pivotal.pal.data.framework.event.SyncEventHandler
import org.springframework.kafka.core.KafkaTemplate

class KafkaSyncEventHandler<C : Any, R : Any?>(private val eventName: String, private val template: KafkaTemplate<Any, C>) : SyncEventHandler<C, R> {

    override fun onEvent(data: C): R? {
        template.send(eventName, data)
        return null
    }
}
