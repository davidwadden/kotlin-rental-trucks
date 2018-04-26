package io.pivotal.pal.data.rentaltruck.reservation.domain

import java.time.LocalDate

data class Reservation(
        val confirmationNumber: String,
        var status: String,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val customerName: String
) {

    fun finalizeConfirmation() {
        status = "FINALIZED"
    }
}
