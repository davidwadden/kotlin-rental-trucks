package io.pivotal.pal.data.rentaltruck.fleet.domain

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.pivotal.pal.data.rentaltruck.loggerFor
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.NoRepositoryBean
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

    @kotlin.jvm.Transient
    @javax.persistence.Transient
    val domainEvents: MutableList<DomainEvent> = mutableListOf()

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false, columnDefinition = "timestamp with time zone")
    lateinit var createdDate: Instant
        private set

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false, columnDefinition = "timestamp with time zone")
    lateinit var lastModifiedDate: Instant
        private set

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

open class EventSourcedTruckRepository(
    private val aggregateRepository: AggregateRepository
) : TruckRepository {

    companion object {
        private val objectMapper = jacksonObjectMapper()
    }

    override fun findByTruckId(truckId: UUID): Truck? {
        val aggregate = aggregateRepository.findByAggregateId(truckId) ?: return null

        return replayEvents(aggregate)
    }

    override fun <S : Truck?> save(entity: S): S {

        var aggregate = aggregateRepository.findByAggregateId(entity?.truckId!!)

        if (aggregate == null) {
            aggregate = AggregateEntity(entity.truckId!!, -1, Truck::class.qualifiedName!!)
        }

        val eventVersion = aggregate
            .events
            .maxBy { event -> event.id.version }
            ?.id
            ?.version ?: -1

        for (i in eventVersion + 1 until entity.domainEvents.size) {

            // serialize event to json
            val domainEvent = entity.domainEvents[i]
            val eventBytes = objectMapper.writeValueAsBytes(domainEvent)
            val eventJson = objectMapper.writeValueAsString(domainEvent)

            loggerFor(EventSourcedTruckRepository::class.java).info("serialized event=$eventJson")

            // append event entity to events relationship on aggregate
            val event = EventEntity(EventEntityKey(entity.truckId!!, i), eventBytes, eventJson)
            aggregate.events.add(event)
        }

        // update current version on aggregate
        aggregate.version = eventVersion + entity.domainEvents.size - (eventVersion + 1)

        // save to event store
        aggregateRepository.save(aggregate)

        return entity
    }

    override fun findAll(): MutableIterable<Truck> {

        return aggregateRepository.findAll()
            .map { replayEvents(it) }
            .toMutableSet()
    }

    override fun deleteById(id: UUID) {
        throw UnsupportedOperationException()
    }

    override fun deleteAll(entities: MutableIterable<Truck>) {
        throw UnsupportedOperationException()
    }

    override fun deleteAll() {
        throw UnsupportedOperationException()
    }

    override fun <S : Truck?> saveAll(entities: MutableIterable<S>): MutableIterable<S> {
        throw UnsupportedOperationException()
    }

    override fun count(): Long = aggregateRepository.findAll().count().toLong()

    override fun findAllById(ids: MutableIterable<UUID>): MutableIterable<Truck> {

        return aggregateRepository.findAllById(ids)
            .map { replayEvents(it) }
            .toMutableSet()
    }

    override fun existsById(id: UUID): Boolean = aggregateRepository.existsById(id)

    override fun findById(id: UUID): Optional<Truck> {
        return aggregateRepository.findById(id)
            .map { replayEvents(it) }
    }

    override fun delete(entity: Truck) {
        throw UnsupportedOperationException()
    }
}

private fun replayEvents(aggregate: AggregateEntity): Truck {
    val truck = Truck()
    aggregate
        .events
        .map { jacksonObjectMapper().readValue(it.data, DomainEvent::class.java) }
        .forEach { truck.handleEvent(it) }

    return truck
}
