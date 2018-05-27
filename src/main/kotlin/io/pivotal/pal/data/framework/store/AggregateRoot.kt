package io.pivotal.pal.data.framework.store

import java.util.*

abstract class AggregateRoot<out A : AggregateRoot<A>> {

    abstract val id: UUID?

    @kotlin.jvm.Transient
    @javax.persistence.Transient
    val domainEvents: MutableList<DomainEvent> = mutableListOf()

    abstract fun handleEvent(event: DomainEvent): A
}
