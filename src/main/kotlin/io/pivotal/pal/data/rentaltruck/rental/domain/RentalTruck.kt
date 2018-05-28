package io.pivotal.pal.data.rentaltruck.rental.domain

import io.pivotal.pal.data.framework.store.AggregateRoot
import io.pivotal.pal.data.framework.store.DomainEvent
import io.pivotal.pal.data.framework.store.EventSourcedRepository
import io.pivotal.pal.data.rentaltruck.fleet.domain.TruckIdFactory
import io.pivotal.pal.data.rentaltruck.loggerFor
import java.util.*

data class RentalTruck internal constructor(
    override var id: UUID?,
    var status: RentalTruckStatus?,
    var mileage: Int?
) : AggregateRoot<RentalTruck>() {

    constructor() : this(
        null,
        null,
        null
    )

    override fun handleEvent(event: DomainEvent): RentalTruck {
        loggerFor(RentalTruck::class.java).info("handling event=$event")

        return when (event) {
            is TruckBoughtEvent -> truckBought(event)
            else -> throw IllegalArgumentException("Unexpected event received by truck: event=$event")
        }
    }

    companion object {

        fun buyTruck(truckIdFactory: TruckIdFactory, mileage: Int): RentalTruck {
            val truck = RentalTruck()
            val truckId = truckIdFactory.makeId()
            val event = TruckBoughtEvent(truckId, mileage)
            return truck.truckBought(event)
        }
    }

    private fun truckBought(event: TruckBoughtEvent): RentalTruck {
        // enqueue domain event
        enqueueEvent(event)

        // apply state mutations
        id = event.truckId
        status = RentalTruckStatus.MAINTENANCE
        mileage = event.mileage

        return this
    }
}

data class TruckBoughtEvent(
    val truckId: UUID,
    val mileage: Int
) : DomainEvent()

enum class RentalTruckStatus {
    AVAILABLE, RENTED, MAINTENANCE
}

typealias RentalTruckRepository = EventSourcedRepository<RentalTruck, UUID>
