package io.pivotal.pal.data.framework.store

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.pivotal.pal.data.rentaltruck.loggerFor
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.Repository
import java.lang.reflect.Constructor
import java.util.*

@Suppress("DeprecatedCallableAddReplaceWith")
@NoRepositoryBean
interface EventSourcedRepository<T, ID> : Repository<T, ID> where T : AggregateRoot<T>, ID : UUID {

    fun <S : T> save(entity: S): S

    fun <S : T> saveAll(entities: MutableIterable<S>): MutableIterable<S>

    fun findById(id: ID): T?

    fun existsById(id: ID): Boolean

    fun findAll(): Iterable<T>

    fun findAllById(ids: MutableIterable<ID>): MutableIterable<T>

    fun count(): Long

    @Deprecated("not available with event sourcing")
    fun deleteById(id: ID) {
        throw UnsupportedOperationException("#delete(entity) not defined for event store")
    }

    @Deprecated("not available with event sourcing")
    fun delete(entity: T) {
        throw UnsupportedOperationException("#delete(entity) not defined for event store")
    }

    @Deprecated("not available with event sourcing")
    fun deleteAll(entities: MutableIterable<T>) {
        throw UnsupportedOperationException("#delete(entity) not defined for event store")
    }

    @Deprecated("not available with event sourcing")
    fun deleteAll() {
        throw UnsupportedOperationException("#delete(entity) not defined for event store")
    }
}

open class EventStoreRepositoryAdapter<T>(
    private val eventStoreRepository: EventStoreRepository
) : EventSourcedRepository<T, UUID> where T : AggregateRoot<T> {

    companion object {
        private val objectMapper = jacksonObjectMapper()
    }

    override fun <S : T> save(entity: S): S {
        loggerFor(EventStoreRepositoryAdapter::class.java).debug("saving entity=$entity")

        // fetch existing aggregate from aggregate repository
        val entityId = entity.id!!
        var aggregate = eventStoreRepository.findByAggregateId(entityId)

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

            loggerFor(EventStoreRepositoryAdapter::class.java).debug("serialized event=$eventJson")

            // append event entity to events relationship on aggregate
            val event = EventEntity(EventEntityKey(entityId, i), eventBytes, eventJson)
            aggregate.events.add(event)
        }

        // update current version on aggregate
        aggregate.version = eventVersion + entity.domainEvents.size - (eventVersion + 1)

        // save to event store
        eventStoreRepository.save(aggregate)

        return entity
    }

    override fun findAll(): MutableIterable<T> {

        return eventStoreRepository.findAll()
            .map { replayEvents(it) }
            .toMutableSet()
    }

    override fun <S : T> saveAll(entities: MutableIterable<S>): MutableIterable<S> {

        return entities
            .map { save(it) }
            .toMutableSet()
    }

    override fun count(): Long = eventStoreRepository.count()

    override fun findAllById(ids: MutableIterable<UUID>): MutableIterable<T> {

        return eventStoreRepository.findAllById(ids)
            .map { replayEvents(it) }
            .toMutableSet()
    }

    override fun existsById(id: UUID): Boolean = eventStoreRepository.existsById(id)

    override fun findById(id: UUID): T? {
        val aggregate = eventStoreRepository.findByAggregateId(id) ?: return null

        return replayEvents(aggregate)
    }

    private fun replayEvents(aggregate: AggregateEntity): T {
        // initialize entity using type field on aggregate table
        val entity: T = instantiateEntity(aggregate.type)

        // guard clause if no events found for given entity
        if (aggregate.events.size == 0) throw IllegalStateException("no events found for aggregate") // FIXME

        // deserialize the data field on each event and send to handleEvent method
        aggregate
            .events
            .map { objectMapper.readValue(it.dataBytes, DomainEvent::class.java) }
            .forEach { entity.handleEvent(it) }

        // return re-hydrated entity by way of replaying event source
        return entity
    }

    @Suppress("UNCHECKED_CAST")
    private fun instantiateEntity(type: String): T {
        val clazz: Class<*> = Class.forName(type)
        val ctor: Constructor<*> = clazz.getConstructor()
        return ctor.newInstance() as T
    }
}
