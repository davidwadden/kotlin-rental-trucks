package io.pivotal.pal.data.framework.store

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

abstract class AggregateRoot<out A : AggregateRoot<A>> {

    abstract val id: UUID?

    @JsonIgnore
    @kotlin.jvm.Transient
    @javax.persistence.Transient
    private val _domainEvents: MutableList<DomainEvent> = mutableListOf()

    val domainEvents: List<DomainEvent>
        get() = Collections.unmodifiableList(_domainEvents)

    abstract fun handleEvent(event: DomainEvent): A

    protected fun enqueueEvent(event: DomainEvent) {
        _domainEvents.add(event)
    }

}
