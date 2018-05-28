package io.pivotal.pal.data.rentaltruck.rental.query

import io.pivotal.pal.data.rentaltruck.rental.domain.Rental
import io.pivotal.pal.data.rentaltruck.rental.domain.RentalRepository
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class ListRentalsQueryHandler(
    private val rentalRepository: RentalRepository
) {

    fun handle(req: ServerRequest): Mono<ServerResponse> {

        val rentalFlux: Flux<Rental> = Flux.fromIterable(rentalRepository.findAll())

        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(rentalFlux, Rental::class.java)
    }
}
