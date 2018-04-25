package io.pivotal.pal.data.framework.event

import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

abstract class AsyncEventChannel(private val eventName: String) {

    protected fun getQueues(): Set<BlockingQueue<*>> {
        return queues.computeIfAbsent(eventName) { _ -> CopyOnWriteArraySet() }
    }

    protected fun addQueue(queue: BlockingQueue<*>) {
        val set = queues.computeIfAbsent(eventName) { _ -> CopyOnWriteArraySet() }
        set.add(queue)
    }

    companion object {

        private val queues = ConcurrentHashMap<String, MutableSet<BlockingQueue<*>>>()
    }
}
