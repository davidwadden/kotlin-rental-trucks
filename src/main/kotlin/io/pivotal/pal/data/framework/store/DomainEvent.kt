package io.pivotal.pal.data.framework.store

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.pivotal.pal.data.rentaltruck.fleet.domain.TruckBoughtEvent
import io.pivotal.pal.data.rentaltruck.fleet.domain.TruckInspectedEvent

// FIXME: Package cycle due to annotations needed for polymorphic deserialization

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    Type(value = TruckBoughtEvent::class, name = "truckBought"),
    Type(value = TruckInspectedEvent::class, name = "truckInspected")
)
abstract class DomainEvent
