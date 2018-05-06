package io.pivotal.pal.data.rentaltruck.reservation.command

import io.pivotal.pal.data.rentaltruck.reservation.domain.Rental
import io.pivotal.pal.data.rentaltruck.reservation.domain.RentalStatus
import io.pivotal.pal.data.rentaltruck.reservation.domain.Reservation
import io.pivotal.pal.data.rentaltruck.reservation.domain.ReservationRepository
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import java.net.URI

internal data class CreateRentalCommandDto(
        val rentalId: String,
        val confirmationNumber: String,
        val truckId: String
)

class CreateRentalCommandHandler(
        private val reservationRepository: ReservationRepository
) {

    fun create(req: ServerRequest): Mono<ServerResponse> {

        return req
                // deserialize the commandDto off the request
                .bodyToMono(CreateRentalCommandDto::class.java)
                // look up associated reservation by confirmation number
                .flatMap<Tuple2<CreateRentalCommandDto, Reservation>> {
                    val res = reservationRepository.findByConfirmationNumber(it.confirmationNumber)
                    Mono.zip(Mono.justOrEmpty(it), Mono.justOrEmpty(res))
                }
                // checks whether reservation has already been rented
                .map { tuple ->
                    if (tuple.t2.rental != null) {
                        throw IllegalStateException("reservation ${tuple.t1.confirmationNumber} is already rented")
                    }
                    return@map tuple
                }
                // create immutable copy of reservation with 1:1 rental added
                .map { tuple -> tuple.t2.createRental(tuple.t1.rentalId, tuple.t1.truckId) }
                // save updated reservation aggregate root to repository
                .map { reservationRepository.save(it) }
                // TODO: could this be served by query-optimized data store?
                // derive hypermedia link to find rental
                .map { reservation ->
                    URI.create("/rentals/${reservation.rental?.rentalId}")
                }
                .flatMap { locationUri ->
                    ServerResponse
                            .created(locationUri)
                            .build()
                }
    }
}
