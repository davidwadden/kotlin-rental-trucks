package io.pivotal.pal.data.rentaltruck.reservation.command

import java.time.LocalDate

data class ReserveTruckCommandDto(
        private val pickupDate: LocalDate,
        private val dropoffDate: LocalDate,
        private val customerName: String
)
