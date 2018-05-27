package io.pivotal.pal.data.rentaltruck.fleet.domain

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.pivotal.pal.data.rentaltruck.loggerFor
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.NoRepositoryBean
import java.util.*

data class Truck(
    var truckId: UUID?,
    var truckName: String?,
    var status: TruckStatus?,
    var mileage: Int?
) {

    constructor() : this(
        null,
        null,
        null,
        null
    )

    @kotlin.jvm.Transient
    @javax.persistence.Transient
    val domainEvents: MutableList<DomainEvent> = mutableListOf()

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
        truckId = event.truckId
        truckName = event.truckName
        status = TruckStatus.MAINTENANCE
        mileage = event.mileage

        return this
    }

    fun inspectTruck(mileage: Int): Truck {
        // TODO: what is a valid business invariant?
        val truckId = truckId ?: throw IllegalStateException("truck not initialized with id")

        return truckInspected(TruckInspectedEvent(truckId, mileage))
    }

    private fun truckInspected(event: TruckInspectedEvent): Truck {
        // enqueue domain event
        domainEvents.add(event)

        // apply state mutations
        mileage = event.mileage

        return this
    }

    fun handleEvent(event: Any): Truck {
        return when (event) {
            is TruckBoughtEvent -> truckBought(event)
            is TruckInspectedEvent -> truckInspected(event)
            else -> throw IllegalArgumentException("Unexpected event received by truck: event=$event")
        }
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

@NoRepositoryBean
interface TruckRepository : CrudRepository<Truck, UUID> {
    fun findByTruckId(truckId: UUID): Truck?
}

