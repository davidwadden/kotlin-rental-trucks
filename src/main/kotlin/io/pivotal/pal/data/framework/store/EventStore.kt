package io.pivotal.pal.data.framework.store

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

    @Column(name = "version", nullable = false, updatable = true)
    var version: Int,

    @Column(name = "type", nullable = false, updatable = false)
    val type: String
) {

    @OneToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(
        name = "aggregate_id",
        referencedColumnName = "aggregate_id",
        nullable = false,
        insertable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "event_aggregate_id_fkey")
    )
    val events: MutableSet<EventEntity> = mutableSetOf()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AggregateEntity

        if (aggregateId != other.aggregateId) return false

        return true
    }

    override fun hashCode(): Int {
        return aggregateId.hashCode()
    }
}

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

    @Column(name = "data", nullable = false, updatable = false, columnDefinition = "bytea")
    val data: ByteArray,

    @Column(name = "char_data", nullable = false, updatable = false, columnDefinition = "text", length = 4096)
    val charData: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventEntity

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

interface AggregateRepository : CrudRepository<AggregateEntity, UUID> {
    fun findByAggregateId(aggregateId: UUID): AggregateEntity?
}
