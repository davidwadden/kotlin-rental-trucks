package io.pivotal.pal.data.rentaltruck.reservation.domain

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.repository.CrudRepository
import java.time.Instant
import java.time.LocalDate
import java.util.*
import javax.persistence.*
import kotlin.streams.asSequence

@Table(
        name = "reservation",
        indexes = [Index(name = "reservation_confirmation_number_key", columnList = "confirmation_number", unique = true)]
)
@Entity
@EntityListeners(AuditingEntityListener::class)
data class Reservation(

        @Id
        @Column(name = "reservation_id", unique = true, nullable = false)
        val reservationId: String,

        @Column(name = "confirmation_number", nullable = true)
        val confirmationNumber: String?,

        @Enumerated(value = EnumType.STRING)
        @Column(name = "status", nullable = false)
        val status: Status,

        @Column(name = "pick_up_date", nullable = false)
        val pickUpDate: LocalDate,

        @Column(name = "drop_off_date", nullable = true)
        val dropOffDate: LocalDate,

        @Column(name = "customer_name", nullable = false)
        val customerName: String
) {

    @CreatedDate
    @Column(name = "created_date", nullable = false, columnDefinition = "timestamp with time zone")
    val createdDate: Instant = Instant.now()

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false, columnDefinition = "timestamp with time zone")
    var lastModifiedDate: Instant? = null
        private set

    fun confirm(): Reservation {
        if (status != Status.CREATED) {
            throw IllegalStateException("can only confirm reservations in created status")
        }

        return copy(
                // set status to confirmed
                status = Status.CONFIRMED,
                // generate confirmation number
                confirmationNumber = generateConfirmationNumber(10)
        )
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
