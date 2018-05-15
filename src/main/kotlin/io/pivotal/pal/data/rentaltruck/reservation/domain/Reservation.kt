package io.pivotal.pal.data.rentaltruck.reservation.domain

import io.pivotal.pal.data.framework.event.AsyncEventPublisher
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

    @kotlin.jvm.Transient
    @javax.persistence.Transient
    private val dirtyEvents = mutableListOf<ReservationEvent>()

    @kotlin.jvm.Transient
    @javax.persistence.Transient
    var domainEvents: List<ReservationEvent> = dirtyEvents
        get() = dirtyEvents.toList()

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
        // append to list of dirty events
        dirtyEvents.add(event)

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

        // apply the state mutation to the aggregate root
        return handleEvent(RentalCreatedEvent(
                reservationId = reservationId,
                rentalId = rentalId,
                confirmationNumber = confirmationNumber,
                rentalStatus = RentalStatus.PENDING,
                pickUpDate = pickUpDate,
                scheduledDropOffDate = dropOffDate,
                customerName = customerName
        ))
    }

    private fun rentalCreated(event: RentalCreatedEvent): Reservation {
        // append to list of dirty events
        dirtyEvents.add(event)

        // create the rental so it can be embedded into the event for potential subscribers
        val newRental = Rental(
                rentalId = event.rentalId,
                confirmationNumber = event.confirmationNumber,
                status = event.rentalStatus,
                reservation = this,
                pickUpDate = event.pickUpDate,
                scheduledDropOffDate = event.scheduledDropOffDate,
                customerName = event.customerName
        )

        // copy jpa entity with reference to updated rental object
        return copy(
                reservationStatus = ReservationStatus.COMPLETED,
                rental = newRental
        )
    }

    fun pickUpRental(truck: Truck): Reservation {
        if (rental == null) {
            throw IllegalStateException("Rental has not been created, cannot pick up rental")
        }
        if (rental.rentalStatus != RentalStatus.PENDING) {
            throw IllegalStateException("Rental is not in the expected Pending rentalStatus.  rentalStatus=${rental.rentalStatus}")
        }

        // delegate business logic to related entities
        val newRental = rental.pickUpRental(truck)

        // apply the state mutation to the aggregate root
        return handleEvent(RentalPickedUpEvent(reservationId, confirmationNumber!!, newRental.rentalStatus, truck.truckId, newRental))
    }

    private fun rentalPickedUp(event: RentalPickedUpEvent): Reservation {
        // append to list of dirty events
        dirtyEvents.add(event)

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
        // append to list of dirty events
        dirtyEvents.add(event)

        return copy(rental = event.rental)
    }

    fun handleEvent(event: ReservationEvent): Reservation {
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

class EventPublishingReservationRepository(
        private val reservationRepository: ReservationRepository,
        private val eventPublisher: AsyncEventPublisher<ReservationEvent>
) : ReservationRepository by reservationRepository {

    override fun <S : Reservation?> save(entity: S): S {
        // delegate to JPA repository
        val retVal = reservationRepository.save(entity)

        // publish domain events
        entity?.domainEvents?.forEach { event -> eventPublisher.publish(event) }

        // return value from delegate
        return retVal
    }

    override fun <S : Reservation?> saveAll(entities: MutableIterable<S>): MutableIterable<S> {
        // delegate to JPA repository
        val retVal = reservationRepository.saveAll(entities)

        // FIXME: does this order make sense?  do we need to order by timestamp
        // publish domain events
        val domainEvents =
                entities.flatMap { it?.domainEvents ?: emptyList() }
        domainEvents.forEach { event -> eventPublisher.publish(event) }

        // return value from delegate
        return retVal
    }
}
