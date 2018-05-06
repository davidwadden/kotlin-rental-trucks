package io.pivotal.pal.data.rentaltruck.reservation.domain

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.time.LocalDate
import javax.persistence.*

@Table(
        name = "rental",
        indexes = [(Index(name = "rental_confirmation_number_key", columnList = "confirmation_number", unique = true))],
        uniqueConstraints = [(UniqueConstraint(name = "rental_reservation_id_key", columnNames = ["reservation_id"]))]
)
@Entity
@EntityListeners(AuditingEntityListener::class)
data class Rental(

        @Id
        @Column(name = "rental_id", nullable = false, updatable = false, length = 36)
        val rentalId: String,

        @Column(name = "confirmation_number", nullable = false, updatable = false, length = 10)
        val confirmationNumber: String,

        @Enumerated(value = EnumType.STRING)
        @Column(name = "status", nullable = false, updatable = true, length = 20)
        val status: RentalStatus,

        @OneToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(
                name = "reservation_id",
                foreignKey = ForeignKey(name = "rental_reservation_id_fkey")
        )
        val reservation: Reservation,

        @Column(name = "truck_id", nullable = false, updatable = false, length = 36)
        val truckId: String,

        @Column(name = "pick_up_date", nullable = false, updatable = false)
        val pickUpDate: LocalDate,

        @Column(name = "scheduled_drop_off_date", nullable = false, updatable = false)
        val scheduledDropOffDate: LocalDate,

        @Column(name = "drop_off_date", nullable = true, updatable = true)
        val dropOffDate: LocalDate?,

        @Column(name = "customer_name", nullable = false, updatable = false, length = 100)
        val customerName: String,

        @Column(name = "dropoff_mileage", nullable = true, updatable = true)
        val dropOffMileage: Int?

) {

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "timestamp with time zone")
    lateinit var createdDate: Instant
        private set

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false, columnDefinition = "timestamp with time zone")
    lateinit var lastModifiedDate: Instant
        private set

}

enum class RentalStatus {
    PENDING, ACTIVE, COMPLETED
}