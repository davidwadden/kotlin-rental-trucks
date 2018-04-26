package io.pivotal.pal.data.rentaltruck.reservation.domain

import org.springframework.stereotype.Component

@Component
class ReservationManager(private val repository: ReservationRepository) {

    fun createReservation(request: ReservationRequest): Reservation {
        val reservation = Reservation(
                confirmationNumber = request.confirmationNumber,
                status = "REQUESTED",
                startDate = request.pickupDate,
                endDate = request.dropoffDate,
                customerName = request.customerName
        )
        repository.save(reservation)

        return reservation
    }

    fun finalizeConfirmation(reservation: Reservation) {
        reservation.finalizeConfirmation()
        repository.save(reservation)
    }

}