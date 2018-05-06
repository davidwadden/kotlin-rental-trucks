package io.pivotal.pal.data.rentaltruck.reservation.command

import io.pivotal.pal.data.rentaltruck.reservation.domain.RentalStatus
import io.pivotal.pal.data.rentaltruck.reservation.domain.Reservation
import io.pivotal.pal.data.rentaltruck.reservation.domain.ReservationRepository
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import java.net.URI
import java.time.LocalDate

internal data class DropOffRentalCommandDto(
        val confirmationNumber: String,
        val dropOffDate: String,
        val dropOffMileage: Int
)

class DropOffRentalCommandHandler(
        private val reservationRepository: ReservationRepository
) {

    fun create(req: ServerRequest): Mono<ServerResponse> {

        return req
                // deserialize the commandDto off the request
                .bodyToMono(DropOffRentalCommandDto::class.java)
                // look up associated reservation by confirmation number
                .flatMap<Tuple2<DropOffRentalCommandDto, Reservation>> {
                    val res = reservationRepository.findByConfirmationNumber(it.confirmationNumber)
                    Mono.zip(Mono.justOrEmpty(it), Mono.justOrEmpty(res))
                }
                // checks whether rental is in the desired state
                .map { tuple ->
                    if (tuple.t2.rental == null) {
                        throw IllegalStateException("reservation ${tuple.t1.confirmationNumber} is not yet rented")
                    }
                    if (tuple.t2.rental!!.status != RentalStatus.ACTIVE) {
                        throw IllegalStateException("rental ${tuple.t1.confirmationNumber} is not currently active, status=${tuple.t2.rental!!.status}")
                    }

                    return@map tuple
                }
                .map { tuple ->
                    val reservation = tuple.t2.dropOffRental(LocalDate.parse(tuple.t1.dropOffDate), tuple.t1.dropOffMileage)
                    return@map reservationRepository.save(reservation)
                }
                .flatMap { _ ->
                    ServerResponse
                            .created(URI.create("stubbed"))
                            .build()
                }
    }
}
