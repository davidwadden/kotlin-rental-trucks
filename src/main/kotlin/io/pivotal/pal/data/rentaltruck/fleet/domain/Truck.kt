package io.pivotal.pal.data.rentaltruck.fleet.domain

import io.pivotal.pal.data.framework.store.AggregateRoot
import io.pivotal.pal.data.framework.store.DomainEvent
import io.pivotal.pal.data.framework.store.EventStoreRepositoryAdapter
import io.pivotal.pal.data.rentaltruck.loggerFor
import java.util.*

data class Truck(
    override var id: UUID?,
    var truckName: String?,
    var status: TruckStatus?,
    var mileage: Int?
) : AggregateRoot<Truck>() {

    constructor() : this(
        null,
        null,
        null,
        null
    )

    override fun handleEvent(event: DomainEvent): Truck {
        loggerFor(Truck::class.java).info("handling event=$event")

        return when (event) {
            is TruckBoughtEvent -> truckBought(event)
            is TruckInspectedEvent -> truckInspected(event)
            else -> throw IllegalArgumentException("Unexpected event received by truck: event=$event")
        }
    }

    companion object {

        fun buyTruck(truckIdFactory: TruckIdFactory, truckName: String, mileage: Int): Truck {
            val truck = Truck()
            val truckId = truckIdFactory.makeId()
            val event = TruckBoughtEvent(truckId, truckName, mileage)
            return truck.truckBought(event)
        }
    }

    private fun truckBought(event: TruckBoughtEvent): Truck {
        // enqueue domain event
        domainEvents.add(event)

        // apply state mutations
        id = event.truckId
        truckName = event.truckName
        status = TruckStatus.MAINTENANCE
        mileage = event.mileage

        return this
    }

    fun inspectTruck(mileage: Int): Truck {
        val truckId = id ?: throw IllegalStateException("truck not initialized with id")

        return truckInspected(TruckInspectedEvent(truckId, mileage))
    }

    private fun truckInspected(event: TruckInspectedEvent): Truck {
        // enqueue domain event
        domainEvents.add(event)

        // apply state mutations
        mileage = event.mileage

        return this
    }
}

interface TruckIdFactory {
    fun makeId(): UUID
}

data class TruckBoughtEvent(
    val truckId: UUID,
    val truckName: String,
    val mileage: Int
) : DomainEvent()

data class TruckInspectedEvent(
    val truckId: UUID,
    val mileage: Int
) : DomainEvent()

enum class TruckStatus {
    AVAILABLE, RENTED, MAINTENANCE
}

typealias TruckRepository = EventStoreRepositoryAdapter<Truck, UUID>
