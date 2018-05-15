package io.pivotal.pal.data.rentaltruck.event

// FIXME: Package cycle
import io.pivotal.pal.data.rentaltruck.reservation.domain.Rental
import java.time.LocalDate

sealed class EventType

data class ReservationConfirmedEvent(
        val reservationId: String,
        val confirmationNumber: String
) : EventType()

data class RentalCreatedEvent(
        val reservationId: String,
        val rental: Rental
) : EventType()

data class RentalPickedUpEvent(
        val reservationId: String,
        val confirmationNumber: String,
        val rental: Rental
) : EventType()

data class RentalDroppedOffEvent(
        val reservationId: String,
        val confirmationNumber: String,
        val rental: Rental,
        val dropOffDate: LocalDate,
        val dropOffMileage: Int
) : EventType()
