package io.pivotal.pal.data.rentaltruck.rental.domain

import io.pivotal.pal.data.framework.store.AggregateRoot
import io.pivotal.pal.data.framework.store.DomainEvent
import io.pivotal.pal.data.framework.store.EventSourcedRepository
import java.time.LocalDate
import java.util.*

data class Rental internal constructor(
    override var id: UUID?,
    var truckId: UUID?,
    var pickUpDate: LocalDate?,
    var dropOffDate: LocalDate?,
    var customerName: String?
) : AggregateRoot<Rental>() {

    constructor() : this(null, null,  null, null, null)

    override fun handleEvent(event: DomainEvent): Rental {

        return when (event) {
            is RentalPickedUpEvent -> rentalPickedUp(event)
            else -> throw IllegalArgumentException()
        }
    }

    companion object {

        fun pickUpRental(rentalIdFactory: RentalIdFactory,
                         truckId: UUID,
                         pickUpDate: LocalDate,
                         dropOffDate: LocalDate,
                         customerName: String): Rental {
            val rental = Rental()
            val rentalId = rentalIdFactory.makeId()
            val event = RentalPickedUpEvent(rentalId, truckId, pickUpDate, dropOffDate, customerName)
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
        dropOffDate = event.dropOffDate
        customerName = event.customerName

        return this
    }
}

data class RentalPickedUpEvent(
    val rentalId: UUID,
    val truckId: UUID,
    val pickUpDate: LocalDate,
    val dropOffDate: LocalDate,
    val customerName: String
) : DomainEvent()

interface RentalIdFactory {
    fun makeId(): UUID
}

typealias RentalRepository = EventSourcedRepository<Rental, UUID>
