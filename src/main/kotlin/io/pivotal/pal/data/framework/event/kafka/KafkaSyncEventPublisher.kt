package io.pivotal.pal.data.framework.event.kafka

import io.pivotal.pal.data.framework.event.DefaultSyncEventPublisher
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.SmartLifecycle
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.listener.config.ContainerProperties

class KafkaSyncEventPublisher<C : Any, R : Any?>(eventName: String, private val consumerProps: Map<String, Any>) : DefaultSyncEventPublisher<C, R>(eventName), MessageListener<Any, C>, SmartLifecycle {

    private val container: KafkaMessageListenerContainer<Any, C>

    init {
        val containerProps = ContainerProperties(eventName)
        containerProps.messageListener = this
        this.container = createContainer(containerProps)
    }

    override fun onMessage(data: ConsumerRecord<Any, C>) {
        publish(data.value())
    }

    override fun isAutoStartup(): Boolean {
        return true
    }

    override fun stop(callback: Runnable) {
        stop()
    }

    override fun start() {
        container.start()
    }

    override fun stop() {
        container.stop()
    }

    override fun isRunning(): Boolean {
        return container.isRunning
    }

    override fun getPhase(): Int {
        return 0
    }

    private fun createContainer(containerProps: ContainerProperties): KafkaMessageListenerContainer<Any, C> {
        val cf = DefaultKafkaConsumerFactory<Any, C>(consumerProps)
        return KafkaMessageListenerContainer(cf, containerProps)
    }

}
