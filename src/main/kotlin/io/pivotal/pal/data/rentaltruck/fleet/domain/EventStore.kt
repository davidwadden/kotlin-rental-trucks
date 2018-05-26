package io.pivotal.pal.data.rentaltruck.fleet.domain

import org.springframework.data.repository.CrudRepository
import java.io.Serializable
import java.util.*
import javax.persistence.*

@Table(name = "aggregate")
@Entity
data class AggregateEntity(

    @Id
    @Column(name = "aggregate_id", nullable = false, updatable = false, length = 36)
    val aggregateId: UUID,

    @Column(name = "version", nullable = false, updatable = false)
    val version: Int,

    @Column(name = "type", nullable = false, updatable = false)
    val type: String,

    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE], orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "aggregate_id",
        referencedColumnName = "aggregate_id",
        nullable = false,
        insertable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "event_aggregate_id_fkey"))
    val events: MutableSet<EventEntity> = mutableSetOf()
)

@Embeddable
data class EventEntityKey(

    @Column(name = "aggregate_id", nullable = false, updatable = false, length = 36)
    val aggregateId: UUID,

    @Column(name = "version", nullable = false, updatable = false)
    val version: Int
) : Serializable

@Table(name = "event")
@Entity
data class EventEntity(

    @EmbeddedId
    val id: EventEntityKey,

    @Lob
    @Column(name = "data", nullable = false, updatable = false)
    val data: String
)

interface AggregateRepository : CrudRepository<AggregateEntity, UUID> {
    fun findByAggregateId(aggregateId: UUID): AggregateEntity?
}
