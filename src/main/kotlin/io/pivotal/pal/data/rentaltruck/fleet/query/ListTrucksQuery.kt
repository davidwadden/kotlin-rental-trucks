package io.pivotal.pal.data.rentaltruck.fleet.query

import io.pivotal.pal.data.rentaltruck.fleet.domain.Truck
import io.pivotal.pal.data.rentaltruck.fleet.domain.TruckRepository
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class ListTrucksQueryHandler(
    private val truckRepository: TruckRepository
) {

    fun handle(req: ServerRequest): Mono<ServerResponse> {

        val truckFlux: Flux<Truck> = Flux.fromIterable(truckRepository.findAll())

        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(truckFlux, Truck::class.java)
    }
}
