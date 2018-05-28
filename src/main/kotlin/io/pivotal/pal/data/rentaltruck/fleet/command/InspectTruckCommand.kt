package io.pivotal.pal.data.rentaltruck.fleet.command

import io.pivotal.pal.data.rentaltruck.fleet.domain.TruckRepository
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.util.*

internal data class InspectTruckCommandDto(
    val mileage: Int
)

class InspectTruckCommandHandler(
    private val truckRepository: TruckRepository
) {

    fun handle(req: ServerRequest): Mono<ServerResponse> {

        val truckId: UUID = UUID.fromString(req.pathVariable("truckId"))

        return req
            .bodyToMono(InspectTruckCommandDto::class.java)
            .map { commandDto ->
                val truck = truckRepository.findByAggregateId(truckId)
                    ?: throw IllegalArgumentException("No truck found for truckId=$truckId")
                Pair(commandDto, truck)
            }
            .map { (commandDto, truck) ->
                truck.inspectTruck(commandDto.mileage)
            }
            .map { truckRepository.save(it) }
            .flatMap {
                ServerResponse
                    .ok()
                    .build()
            }
    }
}