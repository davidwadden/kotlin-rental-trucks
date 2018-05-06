package io.pivotal.pal.data.rentaltruck.reservation.command

import io.pivotal.pal.data.rentaltruck.reservation.domain.Reservation
import io.pivotal.pal.data.rentaltruck.reservation.domain.ReservationRepository
import io.pivotal.pal.data.rentaltruck.reservation.domain.ReservationStatus
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.net.URI
import java.time.Duration
import java.time.LocalDate

internal data class CreateReservationCommandDto(
        val reservationId: String,
        val pickUpDate: String,
        val dropOffDate: String,
        val customerName: String
)

class CreateReservationCommandHandler(
        private val reservationRepository: ReservationRepository
) {

    fun create(req: ServerRequest): Mono<ServerResponse> {

        return req
                .bodyToMono(CreateReservationCommandDto::class.java)
                .map { commandDto -> commandDto.toEntity() }
                .map { reservation -> reservationRepository.save(reservation) }
                .delayElement(Duration.ofSeconds(3L))
                .map { reservation -> reservation.confirm() }
                .map { reservation -> reservationRepository.save(reservation) }
                .map { reservation ->
                    URI.create("/reservations/${reservation.reservationId}")
                }
                .flatMap { locationUri ->
                    ServerResponse
                            .created(locationUri)
                            .build()
                }
    }
}

private fun CreateReservationCommandDto.toEntity(): Reservation =
        Reservation(
                reservationId = reservationId,
                reservationStatus = ReservationStatus.CREATED,
                pickUpDate = LocalDate.parse(pickUpDate),
                dropOffDate = LocalDate.parse(dropOffDate),
                customerName = customerName
        )
