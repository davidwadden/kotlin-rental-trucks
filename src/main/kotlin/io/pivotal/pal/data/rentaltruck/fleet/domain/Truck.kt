package io.pivotal.pal.data.rentaltruck.fleet.domain

import io.pivotal.pal.data.rentaltruck.event.RentalDroppedOff
import io.pivotal.pal.data.rentaltruck.event.RentalPickedUp
import io.pivotal.pal.data.rentaltruck.event.TruckPurchased
import io.pivotal.pal.data.rentaltruck.generateRandomString
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.repository.CrudRepository
import java.time.Instant
import javax.persistence.*


@Table(name = "fleet_truck")
@Entity(name = "fleetTruck")
@EntityListeners(AuditingEntityListener::class)
data class Truck(

        @Id
        @Column(name = "truck_id", nullable = false, updatable = false, length = 36)
        val truckId: String,

        @Enumerated(value = EnumType.STRING)
        @Column(name = "status", nullable = false, updatable = true, length = 20)
        val status: TruckStatus,

        @Column(name = "mileage", nullable = false, updatable = true)
        val mileage: Int,

        @Column(name = "make", nullable = false, updatable = true, length = 50)
        val make: String,

        @Column(name = "model", nullable = false, updatable = true, length = 50)
        val model: String

) {

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "timestamp with time zone")
    lateinit var createdDate: Instant
        private set

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false, columnDefinition = "timestamp with time zone")
    lateinit var lastModifiedDate: Instant
        private set

    fun purchaseTruck(mileage: Int, make: String, model: String): Truck {

        // generate truck id
        val truckId = generateRandomString(6)

        // apply the state mutation to the aggregate root
        return truckPurchased(TruckPurchased(truckId, mileage, make, model))
    }

    private fun truckPurchased(event: TruckPurchased): Truck {
        return Truck(
                truckId = event.truckId,
                status = TruckStatus.AVAILABLE,
                mileage = event.mileage,
                make = event.make,
                model = event.model
        )
    }

    // FIXME: the fleet context should not be familiar with rentals (maybe)
    private fun rentalPickedUp(event: RentalPickedUp): Truck {
        return copy(status = TruckStatus.RENTED)
    }

    private fun rentalDroppedOff(event: RentalDroppedOff): Truck {
        return copy(
                status = TruckStatus.AVAILABLE,
                mileage = event.dropOffMileage
        )
    }

    fun <T> handleEvent(event: T): Truck {
        return when (event) {
            is TruckPurchased -> truckPurchased(event)
            is RentalPickedUp -> rentalPickedUp(event)
            is RentalDroppedOff -> rentalDroppedOff(event)
            else -> throw IllegalArgumentException("Unexpected event type")
        }
    }

}

enum class TruckStatus {
    AVAILABLE, RENTED
}

interface TruckRepository : CrudRepository<Truck, String> {
    fun findByTruckId(truckId: String): Truck?
}
