package io.pivotal.pal.data.rentaltruck.fleet.domain

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*
import javax.persistence.*

@Table(name = "fleet_truck")
@Entity(name = "fleetTruck")
@EntityListeners(AuditingEntityListener::class)
data class Truck(

    @Id
    @Column(name = "truck_id", nullable = false, updatable = false, length = 36)
    var truckId: UUID?,

    @Column(name = "truck_name", nullable = false, updatable = true)
    var truckName: String?,

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false, updatable = true, length = 20)
    var status: TruckStatus?,

    @Column(name = "mileage", nullable = false, updatable = true)
    var mileage: Int?
) {

    constructor() : this(
        null,
        null,
        null,
        null
    )

    companion object {

        fun buyTruck(truckIdFactory: TruckIdFactory, truckName: String, mileage: Int): Truck {
            val truck = Truck()
            val truckId = truckIdFactory.makeId()
            val event = TruckBoughtEvent(truckId, truckName, mileage)
            return truck.truckBought(event)
        }
    }

    private fun truckBought(event: TruckBoughtEvent): Truck {
        truckId = event.truckId
        truckName = event.truckName
        status = TruckStatus.MAINTENANCE
        mileage = event.mileage
        return this
    }

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "timestamp with time zone")
    lateinit var createdDate: Instant
        private set

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false, columnDefinition = "timestamp with time zone")
    lateinit var lastModifiedDate: Instant
        private set
}

interface TruckIdFactory {
    fun makeId(): UUID
}

data class TruckBoughtEvent(
    val truckId: UUID,
    val truckName: String,
    val mileage: Int
)

enum class TruckStatus {
    AVAILABLE, RENTED, MAINTENANCE
}

@Repository("fleetTruckRepository")
interface TruckRepository : CrudRepository<Truck, UUID>
