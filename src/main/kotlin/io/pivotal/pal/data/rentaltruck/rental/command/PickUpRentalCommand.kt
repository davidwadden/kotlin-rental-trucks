package io.pivotal.pal.data.rentaltruck.rental.command

import io.pivotal.pal.data.rentaltruck.rental.domain.Rental
import io.pivotal.pal.data.rentaltruck.rental.domain.RentalIdFactory
import io.pivotal.pal.data.rentaltruck.rental.domain.RentalRepository
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.net.URI
import java.time.LocalDate
import java.util.*

internal data class PickUpRentalCommandDto(
    val truckId: String,
    val pickUpDate: LocalDate,
    val dropOffDate: LocalDate,
    val customerName: String
)

class PickUpRentalCommandHandler(
    private val rentalRepository: RentalRepository,
    private val randomRentalIdFactory: RandomRentalIdFactory
) {

    fun handle(req: ServerRequest): Mono<ServerResponse> {

        return req
            .bodyToMono(PickUpRentalCommandDto::class.java)
            .map { Rental.pickUpRental(randomRentalIdFactory, UUID.fromString(it.truckId), it.pickUpDate, it.dropOffDate, it.customerName) }
            .map { rentalRepository.save(it) }
            .flatMap {
                ServerResponse
                    .created(URI.create("some-location"))
                    .build()
            }
    }
}

class RandomRentalIdFactory : RentalIdFactory {
    override fun makeId(): UUID = UUID.randomUUID()
}
