package io.pivotal.pal.data.framework.event

/**
 * This interface is the common API for asynchronous event publishers.
 *
 * @param <T> type of event
 *
 * @see DefaultAsyncEventPublisher
 */
interface AsyncEventPublisher<T> {

    /**
     * Publishes an event based using the underlying implementation.
     *
     * @param event the event to send
     */
    fun publish(event: T)
}
