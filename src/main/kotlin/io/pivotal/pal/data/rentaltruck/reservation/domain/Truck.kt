package io.pivotal.pal.data.rentaltruck.reservation.domain

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.repository.Repository
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
        val status: TruckStatus,

        @OneToOne(fetch = FetchType.LAZY, optional = true)
        @JoinColumn(
                name = "rental_id",
                foreignKey = ForeignKey(name = "truck_rental_id_fkey")
        )
        val rental: Rental?

) {

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "timestamp with time zone")
    lateinit var createdDate: Instant
        private set

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false, columnDefinition = "timestamp with time zone")
    lateinit var lastModifiedDate: Instant
        private set

    fun assignedToRental(): Truck {
        when (status) {
            TruckStatus.RENTED -> throw IllegalStateException("Truck cannot be assigned.  It is currently Rented.")
            else -> {
                // no-op
            }
        }

        return copy(status = TruckStatus.RENTED)
    }

}

enum class TruckStatus {
    AVAILABLE, RENTED
}

interface TruckRepository : Repository<Truck, String> {
    fun findByTruckId(truckId: String): Truck?
}
