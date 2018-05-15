package io.pivotal.pal.data.rentaltruck.fleet.handler

import io.pivotal.pal.data.framework.event.AsyncEventHandler
import io.pivotal.pal.data.rentaltruck.reservation.domain.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ReservationEventHandler : AsyncEventHandler<ReservationEvent> {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(ReservationEventHandler::class.java)
    }

    override fun onEvent(event: ReservationEvent) {
        // process event types
        when (event) {
            is ReservationConfirmedEvent -> reservationConfirmed(event)
            is RentalCreatedEvent -> rentalCreated(event)
            is RentalPickedUpEvent -> rentalPickedUp(event)
            is RentalDroppedOffEvent -> rentalDroppedOff(event)
        }
    }

    private fun reservationConfirmed(event: ReservationConfirmedEvent) {
        logger.info("reservation confirmed: {}", event)
        // fleet/truck does not care
    }

    private fun rentalCreated(event: RentalCreatedEvent) {
        logger.info("rental created: {}", event)
        // fleet/truck does not care
    }

    private fun rentalPickedUp(event: RentalPickedUpEvent) {
        logger.info("rental picked up: {}", event)
        // set truck to rented status
    }

    private fun rentalDroppedOff(event: RentalDroppedOffEvent) {
        logger.info("rental dropped off: {}", event)
        // set truck to available status
    }
}
