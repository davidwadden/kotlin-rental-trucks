package io.pivotal.pal.data.framework.event

/**
 * Interface to define event subscriber implementations.
 *
 * @param <T> type of event handled
 */
interface AsyncEventHandler<T> {

    /**
     * The event framework will call this method upon processing of an Event.
     *
     * @param event the event
     */
    fun onEvent(event: T)
}
