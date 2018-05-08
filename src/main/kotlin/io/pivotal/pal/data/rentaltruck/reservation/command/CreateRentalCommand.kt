package io.pivotal.pal.data.rentaltruck.reservation.command

import io.pivotal.pal.data.rentaltruck.generateRandomString
import io.pivotal.pal.data.rentaltruck.reservation.domain.Reservation
import io.pivotal.pal.data.rentaltruck.reservation.domain.ReservationRepository
import io.pivotal.pal.data.rentaltruck.reservation.domain.TruckRepository
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import java.net.URI
import java.time.Duration

internal data class CreateRentalCommandDto(
        val confirmationNumber: String,
        val truckId: String
)

class CreateRentalCommandHandler(
        private val reservationRepository: ReservationRepository,
        private val truckRepository: TruckRepository
) {

    fun create(req: ServerRequest): Mono<ServerResponse> {

        val rentalId = generateRandomString(5)

        return req
                // deserialize the commandDto off the request
                .bodyToMono(CreateRentalCommandDto::class.java)
                // look up associated reservation by confirmation number
                .flatMap<Tuple2<CreateRentalCommandDto, Reservation>> {
                    val res = reservationRepository.findByConfirmationNumber(it.confirmationNumber)
                    return@flatMap Mono.zip(Mono.justOrEmpty(it), Mono.justOrEmpty(res))
                }
                // checks whether reservation has already been rented
                .map { tuple ->
                    if (tuple.t2.rental != null) {
                        throw IllegalStateException("reservation ${tuple.t1.confirmationNumber} is already rented")
                    }
                    return@map tuple
                }
                // create immutable copy of reservation with 1:1 rental added
                .map { tuple -> tuple.t2.createRental(rentalId, tuple.t1.truckId) }
                // save updated reservation aggregate root to repository
                .map { reservationRepository.save(it) }
                .delayElement(Duration.ofSeconds(2L))
                .map { reservation -> reservation.pickUpRental() }
                .map { reservationRepository.save(it) }
                // FIXME: ideally the RentalPickedUpEvent handles this
                .map { reservation ->
                    val truck = truckRepository.findByTruckId(reservation.rental!!.truckId)!!
                    truck.pickedUpByRenter()
                    return@map Pair(reservation, truck)
                }
                .map {(reservation, truck) ->
                    truckRepository.save(truck)
                    return@map reservation
                }
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
