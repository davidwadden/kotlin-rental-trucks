package io.pivotal.pal.data.framework.store

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.pivotal.pal.data.rentaltruck.loggerFor
import org.springframework.data.repository.CrudRepository
import java.lang.reflect.Constructor
import java.util.*

class EventStoreRepositoryAdapter<T, ID>(
    private val aggregateRepository: AggregateRepository
) : CrudRepository<T, ID> where T : AggregateRoot<T>, ID : UUID {

    companion object {
        private val objectMapper = jacksonObjectMapper()
    }

    override fun <S : T> save(entity: S): S {
        loggerFor(EventStoreRepositoryAdapter::class.java).info("saving entity=$entity")

        // fetch existing aggregate from aggregate repository
        val entityId = entity.id!!
        var aggregate = aggregateRepository.findByAggregateId(entityId)

        // if does not exist, create using no-arg constructor of S
        if (aggregate == null) {
            aggregate = AggregateEntity(entityId, -1, entity::class.qualifiedName!!)
        }

        // re-calculate current version from aggregate events
        val eventVersion = aggregate
            .events
            .maxBy { event -> event.id.version }
            ?.id
            ?.version ?: -1

        // append dirty domain events to list on aggregate root starting from current version
        for (i in eventVersion + 1 until entity.domainEvents.size) {

            // serialize event to json
            val domainEvent = entity.domainEvents[i]
            val eventBytes = objectMapper.writeValueAsBytes(domainEvent)
            val eventJson = objectMapper.writeValueAsString(domainEvent)

            loggerFor(EventStoreRepositoryAdapter::class.java).info("serialized event=$eventJson")

            // append event entity to events relationship on aggregate
            val event = EventEntity(EventEntityKey(entityId, i), eventBytes, eventJson)
            aggregate.events.add(event)
        }

        // update current version on aggregate
        aggregate.version = eventVersion + entity.domainEvents.size - (eventVersion + 1)

        // save to event store
        aggregateRepository.save(aggregate)

        return entity
    }

    override fun findAll(): MutableIterable<T> {

        return aggregateRepository.findAll()
            .map { replayEvents(it) }
            .toMutableSet()
    }

    override fun deleteById(id: ID) {
        throw UnsupportedOperationException("#deleteById(id) not defined for event store")
    }

    override fun deleteAll(entities: MutableIterable<T>) {
        throw UnsupportedOperationException("#deleteAll(entities) not defined for event store")
    }

    override fun deleteAll() {
        throw UnsupportedOperationException("#deleteAll() not defined for event store")
    }

    override fun <S : T> saveAll(entities: MutableIterable<S>): MutableIterable<S> {

        return entities
            .map { save(it) }
            .toMutableSet()
    }

    override fun count(): Long = aggregateRepository.count()

    override fun findAllById(ids: MutableIterable<ID>): MutableIterable<T> {

        return aggregateRepository.findAllById(ids)
            .map { replayEvents(it) }
            .toMutableSet()
    }

    override fun existsById(id: ID): Boolean = aggregateRepository.existsById(id)

    override fun findById(id: ID): Optional<T> {
        val aggregate = aggregateRepository.findByAggregateId(id) ?: return Optional.empty()

        return Optional.ofNullable(replayEvents(aggregate))
    }

    override fun delete(entity: T) {
        throw UnsupportedOperationException("#delete(entity) not defined for event store")
    }

    fun findByAggregateId(id: ID): T? {
        return findById(id).orElse(null)
    }

    private fun replayEvents(aggregate: AggregateEntity): T {
        // initialize entity using type field on aggregate table
        val entity: T = initializeAggregate(aggregate)

        // guard clause if no events found for given entity
        if (aggregate.events.size == 0) throw IllegalStateException("no events found for aggregate") // FIXME

        // deserialize the data field on each event and send to handleEvent method
        aggregate
            .events
            .map { jacksonObjectMapper().readValue(it.data, DomainEvent::class.java) }
            .forEach { entity.handleEvent(it) }

        // return re-hydrated entity by way of replaying event source
        return entity
    }

    private fun initializeAggregate(aggregate: AggregateEntity): T {
        val clazz: Class<*> = Class.forName(aggregate.type)
        val ctor: Constructor<*> = clazz.getConstructor()
        return ctor.newInstance() as T
    }
}
