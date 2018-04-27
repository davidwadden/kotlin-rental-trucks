package io.pivotal.pal.data.framework.event

import org.slf4j.LoggerFactory
import java.util.concurrent.LinkedBlockingQueue

class AsyncEventSubscriberAdapter<T>(
        eventName: String,
        private val handler: AsyncEventHandler<T>,
        private val errorHandler: AsyncEventHandler<T>? = null
) : AsyncEventChannel(eventName) {
    private val queue = LinkedBlockingQueue<T>()

    init {
        super.addQueue(queue)
        Processor().start()
    }

    private inner class Processor internal constructor() : Thread() {

        override fun run() {

            while (true) {
                var data: T? = null

                try {
                    data = queue.take()

                    logger.debug("calling event handler={}: data={}")
                    handler.onEvent(data)
                } catch (x: Exception) {
                    logger.error("exception thrown in event processor thread", x)

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
