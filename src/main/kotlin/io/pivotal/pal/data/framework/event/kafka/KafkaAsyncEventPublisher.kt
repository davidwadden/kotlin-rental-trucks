package io.pivotal.pal.data.framework.event.kafka

import io.pivotal.pal.data.framework.event.DefaultAsyncEventPublisher
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.context.SmartLifecycle
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.listener.config.ContainerProperties

class KafkaAsyncEventPublisher<T>(eventName: String, private val consumerProps: Map<String, Any>) : DefaultAsyncEventPublisher<T>(eventName), MessageListener<Any, T>, SmartLifecycle {

    private val container: KafkaMessageListenerContainer<Any, T>

    init {
        val containerProps = ContainerProperties(eventName)
        containerProps.messageListener = this
        this.container = createContainer(containerProps)
    }

    override fun onMessage(record: ConsumerRecord<Any, T>) {
        val data = record.value()
        publish(data)
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

    private fun createContainer(containerProps: ContainerProperties): KafkaMessageListenerContainer<Any, T> {
        val cf = DefaultKafkaConsumerFactory<Any, T>(consumerProps)
        return KafkaMessageListenerContainer(cf, containerProps)
    }

}
