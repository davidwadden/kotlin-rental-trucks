package io.pivotal.pal.data.framework.event

import java.util.concurrent.BlockingQueue

open class DefaultAsyncEventPublisher<T>(eventName: String) : AsyncEventChannel(eventName), AsyncEventPublisher<T> {

    override fun publish(data: T) {
        val queues = super.getQueues()

        for (queue in queues) {

            @Suppress("UNCHECKED_CAST")
            (queue as BlockingQueue<T>).offer(data)
        }
    }
}
