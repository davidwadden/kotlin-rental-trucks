package io.pivotal.pal.data.framework.event

import org.springframework.util.Assert

class SyncEventSubscriberAdapter<C, R>(
        eventName: String,
        private val handler: SyncEventHandler<C, R>,
        private val errorHandler: SyncEventHandler<C, R>?,
        private val maxRetryCount: Int,
        private val initialRetryWaitTime: Long,
        private val retryWaitTimeMultiplier: Int,
        private val recoverableExceptions: Set<Class<*>>?
) : SyncEventChannel(eventName) {

    private val lock = java.lang.Object()

    constructor(eventName: String, handler: SyncEventHandler<C, R>) :
            this(eventName, handler, null, 0, 0, 0, null)

    constructor(eventName: String, handler: SyncEventHandler<C, R>, errorHandler: SyncEventHandler<C, R>) :
            this(eventName, handler, errorHandler, 0, 0, 0, null)

    init {
        Assert.hasText(eventName, "Event name must be specified")
        Assert.notNull(handler, "Handler must be specified")

        Assert.isTrue(maxRetryCount >= 0, "Invalid maxRetryCount, must be >=0: $maxRetryCount")

        if (maxRetryCount > 0) {
            Assert.isTrue(initialRetryWaitTime >= 100, "Invalid initialRetryWaitTime, must be >=100: $initialRetryWaitTime")
            Assert.isTrue(retryWaitTimeMultiplier >= 1, "Invalid retryWaitTimeMultiplier, must be >=1: $retryWaitTimeMultiplier")
        }

        @Suppress("UNCHECKED_CAST")
        registerSubscriber(this as SyncEventSubscriberAdapter<Any, Any?>)
    }

    fun onEvent(event: C): R? {
        try {
            var waitTime = initialRetryWaitTime

            for (retryCount in 0..maxRetryCount) {
                try {
                    return handler.onEvent(event)
                } catch (t: Exception) {
                    if (recoverableExceptions != null &&
                            !recoverableExceptions.isEmpty() &&
                            !recoverableExceptions.contains(t.javaClass)) {
                        throw t
                    }

                    if (retryCount < maxRetryCount) {
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
            return errorHandler?.onEvent(event)
        }

    }
}
