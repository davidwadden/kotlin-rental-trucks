package io.pivotal.pal.data.rentaltruck.reservation.domain

import org.springframework.data.repository.CrudRepository
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "reservation", schema = "reservation")
data class Reservation(

        @Id
        @Column(name = "confirmation_number", nullable = false, unique = true)
        val confirmationNumber: String,

        @Column(name = "status", nullable = false)
        var status: String,

        @Column(name = "start_date", nullable = false)
        val startDate: LocalDate,

        @Column(name = "end_date", nullable = false)
        val endDate: LocalDate,

        @Column(name = "customer_name", nullable = false)
        val customerName: String
) {

    fun finalizeConfirmation() {
        status = "FINALIZED"
    }
}

interface ReservationRepository : CrudRepository<Reservation, String>
