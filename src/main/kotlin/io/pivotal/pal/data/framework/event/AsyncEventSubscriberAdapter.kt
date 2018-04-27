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
        private val retryWaitTimeMultiplier: Int
) : AsyncEventChannel(eventName), SmartLifecycle {

    private val queue = LinkedBlockingQueue<T>()
    private var running = false

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

    private inner class Processor internal constructor() : Thread() {

        private val lock = java.lang.Object()

        override fun run() {

            while (running) {
                var data: T? = null

                try {
                    data = queue.take()
                    var waitTime = initialRetryWaitTime

                    for (i in 0..maxRetryCount) {
                        logger.debug("calling event handler={}: data={}", handler, data)

                        try {
                            handler.onEvent(data)
                            break
                        } catch (e: Exception) {
                            if (i < maxRetryCount) {
                                try {
                                    synchronized(lock) {
                                        lock.wait(waitTime)
                                    }

                                    waitTime *= retryWaitTimeMultiplier.toLong()
                                } catch (t: InterruptedException) {
                                    // no-op
                                }

                            } else {
                                throw e
                            }
                        }

                    }
                } catch (x: Exception) {
                    logger.error("exception thrown in event processor thread: x={}, data={}", x.toString(), data, x)

                    if (errorHandler != null && data != null) {
                        errorHandler.onEvent(data)
                    }
                }

            }
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(AsyncEventSubscriberAdapter::class.java)
    }
}
