package io.pivotal.pal.data.rentaltruck.rental.domain

import io.pivotal.pal.data.framework.store.AggregateRoot
import io.pivotal.pal.data.framework.store.DomainEvent
import io.pivotal.pal.data.framework.store.EventSourcedRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class Rental internal constructor(
    override var id: UUID?,
    var truckId: UUID?,
    var pickUpDate: LocalDate?,
    var dropOffDateTime: LocalDateTime?,
    var customerName: String?
) : AggregateRoot<Rental>() {

    constructor() : this(null, null, null, null, null)

    override fun handleEvent(event: DomainEvent): Rental {

        return when (event) {
            is RentalPickedUpEvent -> rentalPickedUp(event)
            is RentalDroppedOffEvent -> rentalDroppedOff(event)
            else -> throw IllegalArgumentException()
        }
    }

    companion object {

        fun pickUpRental(rentalIdFactory: RentalIdFactory,
                         truckId: UUID,
                         pickUpDate: LocalDate,
                         customerName: String): Rental {
            val rental = Rental()
            val rentalId = rentalIdFactory.makeId()
            val event = RentalPickedUpEvent(rentalId, truckId, pickUpDate, customerName)
            return rental.rentalPickedUp(event)
        }

    }

    private fun rentalPickedUp(event: RentalPickedUpEvent): Rental {
        // enqueue event
        enqueueEvent(event)
        // update state of entity
        id = event.rentalId
        truckId = event.truckId
        pickUpDate = event.pickUpDate
        customerName = event.customerName

        return this
    }

    fun dropOffRental(truckId: UUID, dropOffDateTime: LocalDateTime): Rental {
        val event = RentalDroppedOffEvent(id!!, truckId, dropOffDateTime)
        return rentalDroppedOff(event)
    }

    private fun rentalDroppedOff(event: RentalDroppedOffEvent): Rental {
        // enqueue event
        enqueueEvent(event)
        // update state of entity
        dropOffDateTime = event.dropOffDateTime

        return this
    }
}

data class RentalPickedUpEvent(
    val rentalId: UUID,
    val truckId: UUID,
    val pickUpDate: LocalDate,
    val customerName: String
) : DomainEvent()

data class RentalDroppedOffEvent(
    val rentalId: UUID,
    val truckId: UUID,
    val dropOffDateTime: LocalDateTime
) : DomainEvent()

interface RentalIdFactory {
    fun makeId(): UUID
}

typealias RentalRepository = EventSourcedRepository<Rental, UUID>
