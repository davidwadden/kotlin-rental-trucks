package io.pivotal.pal.data.rentaltruck.reservation.domain

import io.pivotal.pal.data.rentaltruck.generateRandomString
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.repository.CrudRepository
import java.time.Instant
import java.time.LocalDate
import javax.persistence.*

@Table(
        name = "reservation",
        indexes = [Index(name = "reservation_confirmation_number_key", columnList = "confirmation_number", unique = true)]
)
@Entity
@EntityListeners(AuditingEntityListener::class)
data class Reservation private constructor(

        @Id
        @Column(name = "reservation_id", unique = true, nullable = false, updatable = false, length = 36)
        val reservationId: String,

        @Column(name = "confirmation_number", nullable = true, updatable = true, length = 10)
        val confirmationNumber: String?,

        @Enumerated(value = EnumType.STRING)
        @Column(name = "reservationStatus", nullable = false, updatable = true, length = 20)
        val reservationStatus: ReservationStatus,

        @OneToOne(
                mappedBy = "reservation",
                cascade = [CascadeType.ALL],
                fetch = FetchType.LAZY,
                optional = true
        )
        val rental: Rental?,

        @Column(name = "pick_up_date", nullable = false, updatable = false)
        val pickUpDate: LocalDate,

        @Column(name = "drop_off_date", nullable = true, updatable = false)
        val dropOffDate: LocalDate,

        @Column(name = "customer_name", nullable = false, updatable = false, length = 100)
        val customerName: String
) {

    constructor(
            reservationId: String,
            reservationStatus: ReservationStatus,
            pickUpDate: LocalDate,
            dropOffDate: LocalDate,
            customerName: String
    ) : this(
            reservationId = reservationId,
            confirmationNumber = null,
            reservationStatus = reservationStatus,
            rental = null,
            pickUpDate = pickUpDate,
            dropOffDate = dropOffDate,
            customerName = customerName
    )

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "timestamp with time zone")
    lateinit var createdDate: Instant
        private set

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false, columnDefinition = "timestamp with time zone")
    lateinit var lastModifiedDate: Instant
        private set

    fun confirm(): Reservation {
        if (reservationStatus != ReservationStatus.CREATED) {
            throw IllegalStateException("can only confirm reservations in created reservationStatus")
        }

        return copy(
                // set status to confirmed
                reservationStatus = ReservationStatus.CONFIRMED,
                // generate confirmation number
                confirmationNumber = generateRandomString(6)
        )
    }

    // TODO: Generate rentalId instead of asking caller to provide
    fun createRental(rentalId: String): Reservation {
        if (rental != null) {
            throw IllegalStateException("Rental already exists.  Reservation must not have rental")
        }
        if (confirmationNumber == null) {
            throw IllegalStateException("Confirmation number should not be null.")
        }
        if (reservationStatus != ReservationStatus.CONFIRMED) {
            throw IllegalStateException("Expected reservation status to be Confirmed.  status=$reservationStatus")
        }

        val newRental = Rental(
                rentalId = rentalId,
                confirmationNumber = confirmationNumber,
                status = RentalStatus.PENDING,
                reservation = this,
                pickUpDate = pickUpDate,
                scheduledDropOffDate = dropOffDate,
                customerName = customerName
        )

        return copy(reservationStatus = ReservationStatus.COMPLETED, rental = newRental)
    }

    fun pickUpRental(truck: Truck): Reservation {
        if (rental == null) {
            throw IllegalStateException("Rental has not been created, cannot pick up rental")
        }
        if (rental.status != RentalStatus.PENDING) {
            throw IllegalStateException("Rental is not in the expected Pending status.  status=${rental.status}")
        }

        val newRental = rental.pickUpRental(truck)

        return copy(rental = newRental)
    }

    fun dropOffRental(dropOffDate: LocalDate, dropOffMileage: Int): Reservation {
        if (rental == null) {
            throw IllegalStateException("Rental does not exist.  Cannot drop off.")
        }

        val newRental = rental.dropOffRental(dropOffDate, dropOffMileage)

        return copy(rental = newRental)
    }
}

enum class ReservationStatus {
    CREATED, CONFIRMED, COMPLETED, CANCELED
}

interface ReservationRepository : CrudRepository<Reservation, String> {
    fun findByConfirmationNumber(confirmationNumber: String): Reservation?
}
