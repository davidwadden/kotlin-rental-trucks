package io.pivotal.pal.data.framework.store

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*

abstract class AggregateRoot<out A : AggregateRoot<A>> {

    abstract val id: UUID?

    @JsonIgnore
    @kotlin.jvm.Transient
    @javax.persistence.Transient
    val domainEvents: MutableList<DomainEvent> = mutableListOf()

    abstract fun handleEvent(event: DomainEvent): A
}
