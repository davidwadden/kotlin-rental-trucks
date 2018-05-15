package io.pivotal.pal.data.framework.event

open class DefaultSyncEventPublisher<C : Any, R : Any?>(eventName: String) : SyncEventChannel(eventName), SyncEventPublisher<C, R> {

    override fun publish(event: C): R? {
        val set = getSubscribers()
        var retValue: R? = null

        for (subscriber in set) {

            @Suppress("UNCHECKED_CAST")
            retValue = subscriber.onEvent(event) as R?
        }

        return retValue
    }
}
