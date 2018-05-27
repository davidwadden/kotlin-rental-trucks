package io.pivotal.pal.data.rentaltruck.fleet.domain

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.pivotal.pal.data.rentaltruck.loggerFor
import java.util.*

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
