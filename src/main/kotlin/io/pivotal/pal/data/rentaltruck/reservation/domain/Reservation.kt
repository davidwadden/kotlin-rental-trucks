package io.pivotal.pal.data.rentaltruck.reservation.domain

import io.pivotal.pal.data.rentaltruck.event.*
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

    // NOTE: creation of the Reservation does not emit an event for now since that requires initializing
    // in a "null" state to record a ReservationCreated domain event to initalize the state.  Deferring
    // this until we decide whether to event-source the Reservation.
    constructor(
            reservationId: String,
            pickUpDate: LocalDate,
            dropOffDate: LocalDate,
            customerName: String
    ) : this(
            reservationId = reservationId,
            confirmationNumber = null,
            reservationStatus = ReservationStatus.CREATED,
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

        // generate confirmation number
        val confirmationNumber = generateRandomString(6)
        return reservationConfirmed(ReservationConfirmedEvent(reservationId, confirmationNumber))
    }

    private fun reservationConfirmed(event: ReservationConfirmedEvent): Reservation {
        return copy(
                // set status to confirmed
                reservationStatus = ReservationStatus.CONFIRMED,
                confirmationNumber = event.confirmationNumber
        )
    }

    fun createRental(): Reservation {
        if (rental != null) {
            throw IllegalStateException("Rental already exists.  Reservation must not have rental")
        }
        if (confirmationNumber == null) {
            throw IllegalStateException("Confirmation number should not be null.")
        }
        if (reservationStatus != ReservationStatus.CONFIRMED) {
            throw IllegalStateException("Expected reservation status to be Confirmed.  status=$reservationStatus")
        }

        // generate confirmation number
        val rentalId = generateRandomString(6)

        // create the rental so it can be embedded into the event for potential subscribers
        val newRental = Rental(
                rentalId = rentalId,
                confirmationNumber = confirmationNumber,
                status = RentalStatus.PENDING,
                reservation = this,
                pickUpDate = pickUpDate,
                scheduledDropOffDate = dropOffDate,
                customerName = customerName
        )

        // apply the state mutation to the aggregate root
        return handleEvent(RentalCreatedEvent(reservationId = reservationId, rental = newRental))
    }

    private fun rentalCreated(event: RentalCreatedEvent): Reservation {
        return copy(
                reservationStatus = ReservationStatus.COMPLETED,
                rental = event.rental
        )
    }

    fun pickUpRental(truck: Truck): Reservation {
        if (rental == null) {
            throw IllegalStateException("Rental has not been created, cannot pick up rental")
        }
        if (rental.status != RentalStatus.PENDING) {
            throw IllegalStateException("Rental is not in the expected Pending status.  status=${rental.status}")
        }

        // delegate business logic to related entities
        val newRental = rental.pickUpRental(truck)

        // apply the state mutation to the aggregate root
        return handleEvent(RentalPickedUpEvent(reservationId, confirmationNumber!!, newRental)) // FIXME
    }

    private fun rentalPickedUp(event: RentalPickedUpEvent): Reservation {
        return copy(rental = event.rental)
    }

    fun dropOffRental(dropOffDate: LocalDate, dropOffMileage: Int): Reservation {
        if (rental == null) {
            throw IllegalStateException("Rental does not exist.  Cannot drop off.")
        }

        // delegate business logic to related entities
        val newRental = rental.dropOffRental(dropOffDate, dropOffMileage)

        // apply the state mutation to the aggregate root
        return handleEvent(RentalDroppedOffEvent(reservationId, confirmationNumber!!, newRental, dropOffDate, dropOffMileage))
    }

    private fun rentalDroppedOff(event: RentalDroppedOffEvent): Reservation {
        return copy(rental = event.rental)
    }

    fun handleEvent(event: EventType): Reservation {
        return when (event) {
            is ReservationConfirmedEvent -> reservationConfirmed(event)
            is RentalCreatedEvent -> rentalCreated(event)
            is RentalPickedUpEvent -> rentalPickedUp(event)
            is RentalDroppedOffEvent -> rentalDroppedOff(event)
        }
    }
}

enum class ReservationStatus {
    CREATED, CONFIRMED, COMPLETED, CANCELED
}

interface ReservationRepository : CrudRepository<Reservation, String> {
    fun findByConfirmationNumber(confirmationNumber: String): Reservation?
}
