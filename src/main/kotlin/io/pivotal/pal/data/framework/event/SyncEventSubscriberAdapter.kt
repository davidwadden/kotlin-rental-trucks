package io.pivotal.pal.data.framework.event

class SyncEventSubscriberAdapter<C, R>(
        eventName: String,
        private val handler: SyncEventHandler<C, R>,
        private val errorHandler: SyncEventHandler<C, R>?,
        private val maxRetryCount: Int,
        private val initialRetryWaitTime: Long,
        private val retryWaitTimeMultiplier: Int
) : SyncEventChannel(eventName) {

    private val lock = java.lang.Object()

    init {

        @Suppress("UNCHECKED_CAST")
        registerSubscriber(this as SyncEventSubscriberAdapter<Any, Any?>)
    }

    fun onEvent(data: C): R? {
        try {
            var waitTime = initialRetryWaitTime

            for (i in 0..maxRetryCount) {
                try {
                    return handler.onEvent(data)
                } catch (t: Exception) {
                    if (i < maxRetryCount) {
                        synchronized(lock) {
                            lock.wait(waitTime)
                        }
                        waitTime *= retryWaitTimeMultiplier.toLong()
                    } else {
                        throw t
                    }
                }

            }

            return null
        } catch (e: Exception) {
            return errorHandler?.onEvent(data)
        }

    }
}
