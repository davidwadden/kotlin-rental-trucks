package io.pivotal.pal.data.rentaltruck.reservation.command

import io.pivotal.pal.data.rentaltruck.reservation.domain.Reservation
import io.pivotal.pal.data.rentaltruck.reservation.domain.ReservationRepository
import io.pivotal.pal.data.rentaltruck.reservation.domain.Status
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.net.URI
import java.time.LocalDate

data class CreateReservationCommandDto(
        val reservationId: String,
        val pickUpDate: String,
        val dropOffDate: String,
        val customerName: String
)

class CreateReservationCommandHandler(private val reservationRepository: ReservationRepository) {

    fun create(req: ServerRequest): Mono<ServerResponse> {

        return req
                .bodyToMono(CreateReservationCommandDto::class.java)
                .map { dto -> dto.toEntity() }
                .map { entity -> reservationRepository.save(entity) }
                .map { entity -> entity.confirm() }
                .map { entity -> reservationRepository.save(entity) }
                .flatMap { _ ->
                    ServerResponse
                            .created(URI.create("/reservations/stubbed"))
                            .build()
                }
    }
}

private fun CreateReservationCommandDto.toEntity(): Reservation =
        Reservation(
                reservationId = reservationId,
                confirmationNumber = null,
                status = Status.CREATED,
                pickUpDate = LocalDate.parse(pickUpDate),
                dropOffDate = LocalDate.parse(dropOffDate),
                customerName = customerName
        )
