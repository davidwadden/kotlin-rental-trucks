package io.pivotal.pal.data.framework.event

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet


abstract class SyncEventChannel protected constructor(protected val eventName: String) {

    protected fun getSubscribers(): Set<SyncEventSubscriberAdapter<Any, Any?>> {
        return subscribers[eventName] ?: throw IllegalArgumentException("Subscriber for event $eventName not found")
    }

    protected fun registerSubscriber(subscriber: SyncEventSubscriberAdapter<Any, Any?>) {
        val set = subscribers.computeIfAbsent(eventName) { _ -> CopyOnWriteArraySet<SyncEventSubscriberAdapter<Any, Any?>>() }
        set.add(subscriber)
    }

    companion object {

        private val subscribers = ConcurrentHashMap<String, MutableSet<SyncEventSubscriberAdapter<Any, Any?>>>()
    }
}
