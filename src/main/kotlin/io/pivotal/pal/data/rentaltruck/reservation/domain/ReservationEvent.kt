package io.pivotal.pal.data.rentaltruck.reservation.domain

// FIXME: Package cycle
import java.time.LocalDate

sealed class ReservationEvent

data class ReservationConfirmedEvent(
        val reservationId: String,
        val confirmationNumber: String
) : ReservationEvent()

data class RentalCreatedEvent(
        val reservationId: String,
        val rentalId: String,
        val confirmationNumber: String,
        val rentalStatus: RentalStatus,
        val pickUpDate: LocalDate,
        val scheduledDropOffDate: LocalDate,
        val customerName: String
) : ReservationEvent()

data class RentalPickedUpEvent(
        val reservationId: String,
        val confirmationNumber: String,
        val rentalStatus: RentalStatus,
        val truckId: String,
        val rental: Rental
) : ReservationEvent()

data class RentalDroppedOffEvent(
        val reservationId: String,
        val confirmationNumber: String,
        val rental: Rental,
        val dropOffDate: LocalDate,
        val dropOffMileage: Int
) : ReservationEvent()
