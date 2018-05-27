package io.pivotal.pal.data.rentaltruck.fleet.domain

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo

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
