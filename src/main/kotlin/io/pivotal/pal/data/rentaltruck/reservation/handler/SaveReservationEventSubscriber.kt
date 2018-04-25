package io.pivotal.pal.data.rentaltruck.reservation.handler

import io.pivotal.pal.data.framework.event.AsyncEventHandler
import io.pivotal.pal.data.rentaltruck.event.ReservationRequestedEvent
import org.springframework.stereotype.Component

@Component
class SaveReservationEventSubscriber : AsyncEventHandler<ReservationRequestedEvent> {

    override fun onEvent(data: ReservationRequestedEvent) {
        // save reservation to database
    }
}
