package io.pivotal.pal.data.rentaltruck.reservation.domain

import java.time.LocalDate

data class ReservationRequest(
        val pickupDate: LocalDate,
        val dropoffDate: LocalDate,
        val customerName: String,
        val confirmationNumber: String
)
