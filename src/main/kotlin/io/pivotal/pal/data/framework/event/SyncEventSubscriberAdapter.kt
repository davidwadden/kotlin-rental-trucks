package io.pivotal.pal.data.framework.event

class SyncEventSubscriberAdapter<C : Any, R : Any?>(eventName: String, private val handler: SyncEventHandler<C, R>) : SyncEventChannel(eventName) {

    init {
        @Suppress("UNCHECKED_CAST")
        registerSubscriber(this as SyncEventSubscriberAdapter<Any, Any?>)
    }

    fun onEvent(data: C): R? {
        return handler.onEvent(data)
    }
}
