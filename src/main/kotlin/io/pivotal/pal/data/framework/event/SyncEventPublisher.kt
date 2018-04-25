package io.pivotal.pal.data.framework.event

/**
 * This interface is the common API for synchronous event publishers.
 *
 * @param <C> command type
 * @param <R> return type
 */
interface SyncEventPublisher<C, R> {

    /**
     * Publishes an event based using the underlying implementation.
     *
     * @param data the event to send
     * @return return value from processing the event
     */
    fun publish(data: C): R?
}
