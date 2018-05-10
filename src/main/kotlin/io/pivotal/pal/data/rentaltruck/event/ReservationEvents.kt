package io.pivotal.pal.data.rentaltruck.event

// FIXME: package cycle
import io.pivotal.pal.data.rentaltruck.reservation.domain.Rental
import java.time.LocalDate

data class ReservationConfirmed(
        val reservationId: String,
        val confirmationNumber: String
)

data class RentalCreated(
        val reservationId: String,
        val rental: Rental
)

data class RentalPickedUp(
        val reservationId: String,
        val confirmationNumber: String,
        val rental: Rental
)

data class RentalDroppedOff(
        val reservationId: String,
        val confirmationNumber: String,
        val rental: Rental,
        val dropOffDate: LocalDate,
        val dropOffMileage: Int
)
