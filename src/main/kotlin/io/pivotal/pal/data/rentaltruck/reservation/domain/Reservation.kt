package io.pivotal.pal.data.rentaltruck.reservation.domain

import org.springframework.data.repository.CrudRepository
import java.time.LocalDate
import java.util.*
import javax.persistence.*
import kotlin.streams.asSequence

@Entity
data class Reservation(

        @Id
        @Column(name = "reservation_id", unique = true, nullable = false)
        val reservationId: String,

        @Column(name = "confirmation_number", unique = true, nullable = true)
        var confirmationNumber: String?,

        @Enumerated(value = EnumType.STRING)
        @Column(name = "status")
        var status: Status,

        @Column(name = "pick_up_date")
        val pickUpDate: LocalDate,

        @Column(name = "drop_off_date")
        val dropOffDate: LocalDate,

        @Column(name = "customer_name")
        val customerName: String
) {

    fun confirm(): Reservation {
        // set status to confirmed
        this.status = Status.CONFIRMED
        // generate confirmation number
        this.confirmationNumber = generateConfirmationNumber(10)

        return this
    }
}

enum class Status {
    CREATED, CONFIRMED, COMPLETED, CANCELED
}

interface ReservationRepository : CrudRepository<Reservation, String>

private fun generateConfirmationNumber(outputLength: Long): String {
    val source = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    return Random()
            .ints(outputLength, 0, source.length)
            .asSequence()
            .map(source::get)
            .joinToString("")
}
