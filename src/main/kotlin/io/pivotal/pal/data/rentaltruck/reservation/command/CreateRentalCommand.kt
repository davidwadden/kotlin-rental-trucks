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
                // build rental (child) and chain on with reservation (parent)
                .map { tuple ->
                    val rental = Rental(
                            rentalId = tuple.t1.rentalId,
                            confirmationNumber = tuple.t1.confirmationNumber,
                            status = RentalStatus.PENDING,
                            reservation = tuple.t2,
                            truckId = tuple.t1.truckId,
                            pickUpDate = tuple.t2.pickUpDate,
                            scheduledDropOffDate = tuple.t2.dropOffDate,
                            dropOffDate = null,
                            customerName = tuple.t2.customerName,
                            dropOffMileage = null
                    )
                    Pair(rental, tuple.t2)
                }
                // create immutable copy of reservation with 1:1 rental added
                .map { (rental, reservation) ->
                    reservation.copy(rental = rental)
                }
                .map { reservationRepository.save(it) }
                .map { reservation ->
                    URI.create("/rentals/${reservation.rental?.rentalId}")
                }
                .flatMap {locationUri ->
                    ServerResponse
                            .created(locationUri)
                            .build()
                }
    }
}
