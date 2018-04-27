package io.pivotal.pal.data.framework.event

import org.slf4j.LoggerFactory
import org.springframework.context.SmartLifecycle
import org.springframework.util.Assert
import java.util.concurrent.LinkedBlockingQueue

class AsyncEventSubscriberAdapter<T>(
        eventName: String,
        private val handler: AsyncEventHandler<T>,
        private val errorHandler: AsyncEventHandler<T>?,
        private val maxRetryCount: Int,
        private val initialRetryWaitTime: Long,
        private val retryWaitTimeMultiplier: Int,
        private val recoverableExceptions: Set<Class<*>>?
) : AsyncEventChannel(eventName), SmartLifecycle {

    constructor(eventName: String, handler: AsyncEventHandler<T>) :
            this(eventName, handler, null, 0, 0, 0, null)

    constructor(eventName: String, handler: AsyncEventHandler<T>, errorHandler: AsyncEventHandler<T>) :
            this(eventName, handler, errorHandler, 0, 0, 0, null)

    private val queue = LinkedBlockingQueue<T>()
    private var running = false
    private val lock = java.lang.Object()

    init {

        Assert.isTrue(maxRetryCount >= 0, "Invalid maxRetryCount: $maxRetryCount")

        super.addQueue(queue)
    }

    override fun isAutoStartup(): Boolean {
        return true
    }

    override fun stop(callback: Runnable) {
        running = false
    }

    override fun start() {
        running = true
        Processor().start()
    }

    override fun stop() {
        running = false
    }

    override fun isRunning(): Boolean {
        return running
    }

    override fun getPhase(): Int {
        return 0
    }

    private fun processEntry() {
        var data: T? = null

        try {
            data = queue.take()
            var waitTime = initialRetryWaitTime

            for (retryCount in 0..maxRetryCount) {
                logger.debug("calling event handler={}: data={}", handler, data)

                try {
                    handler.onEvent(data)
                    break
                } catch (e: Exception) {
                    waitTime = handleException(e, waitTime, retryCount)
                }

            }
        } catch (x: Exception) {
            logger.error("exception thrown in event processor thread: x={}, data={}", x.toString(), data, x)

            if (errorHandler != null && data != null) {
                errorHandler.onEvent(data)
            }
        }

    }

    @Throws(Exception::class)
    private fun handleException(e: Exception, waitTime: Long, retryCount: Int): Long {
        var waitTime = waitTime
        if (recoverableExceptions != null &&
                !recoverableExceptions.isEmpty() &&
                !recoverableExceptions.contains(e.javaClass)) {
            // if recoverable exceptions specified and this exception is not recoverable, rethrow
            throw e
        }

        if (retryCount < maxRetryCount) {
            try {
                synchronized(lock) {
                    lock.wait(waitTime)
                }

                waitTime *= retryWaitTimeMultiplier.toLong()
            } catch (t: InterruptedException) {
                // no-op
            }

            return waitTime
        } else {
            throw e
        }
    }

    private inner class Processor internal constructor() : Thread() {

        override fun run() {

            while (running) {
                processEntry()
            }
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(AsyncEventSubscriberAdapter::class.java)
    }
}
