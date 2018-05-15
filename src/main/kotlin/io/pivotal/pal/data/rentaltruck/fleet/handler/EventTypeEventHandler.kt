package io.pivotal.pal.data.rentaltruck.fleet.handler

import io.pivotal.pal.data.framework.event.AsyncEventHandler
import io.pivotal.pal.data.rentaltruck.event.EventType

class EventTypeEventHandler : AsyncEventHandler<EventType> {

    override fun onEvent(event: EventType) {
        // process event types
    }
}
