package io.pivotal.pal.data.rentaltruck.event

import java.time.LocalDate

data class ReservationRequestedEvent(
        val pickupDate: LocalDate,
        val dropoffDate: LocalDate,
        val customerName: String,
        val confirmationNumber: String
)
