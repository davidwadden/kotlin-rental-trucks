package io.pivotal.pal.data.rentaltruck.reservation.command

import io.pivotal.pal.data.rentaltruck.reservation.domain.Rental
import io.pivotal.pal.data.rentaltruck.reservation.domain.RentalStatus
import io.pivotal.pal.data.rentaltruck.reservation.domain.ReservationRepository
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
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
                .map {
                    val res = reservationRepository.findByConfirmationNumber(it.confirmationNumber)
                    Pair(it, res)
                }
                // business invariant: prevent rentals with invalid confirmation numbers
                .map { (commandDto, res) ->
                    if (res == null) {
                        throw IllegalArgumentException()
                    }
                    Pair(commandDto, res)
                }
                // build rental (child) and chain on with reservation (parent)
                .map { (commandDto, reservation) ->
                    val rental = Rental(
                            rentalId = commandDto.rentalId,
                            confirmationNumber = commandDto.confirmationNumber,
                            status = RentalStatus.PENDING,
                            reservation = reservation,
                            truckId = commandDto.truckId,
                            pickUpDate = reservation.pickUpDate,
                            scheduledDropOffDate = reservation.dropOffDate,
                            dropOffDate = null,
                            customerName = reservation.customerName,
                            dropOffMileage = null
                    )
                    Pair(rental, reservation)
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
