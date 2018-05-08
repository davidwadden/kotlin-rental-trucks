package io.pivotal.pal.data.rentaltruck.reservation.domain

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.repository.CrudRepository
import java.time.Instant
import javax.persistence.*

@Table(name = "truck")
@Entity
@EntityListeners(AuditingEntityListener::class)
data class Truck(

        @Id
        @Column(name = "truck_id", nullable = false, updatable = false, length = 36)
        val truckId: String,

        @Enumerated(value = EnumType.STRING)
        @Column(name = "status", nullable = false, updatable = true, length = 20)
        val status: TruckStatus

) {

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "timestamp with time zone")
    lateinit var createdDate: Instant
        private set

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false, columnDefinition = "timestamp with time zone")
    lateinit var lastModifiedDate: Instant
        private set

    fun pickedUpByRenter(): Truck {
        return copy(status = TruckStatus.RENTED)
    }

}

enum class TruckStatus {
    AVAILABLE, RENTED
}

interface TruckRepository : CrudRepository<Truck, String> {
    fun findByTruckId(truckId: String): Truck?
}
