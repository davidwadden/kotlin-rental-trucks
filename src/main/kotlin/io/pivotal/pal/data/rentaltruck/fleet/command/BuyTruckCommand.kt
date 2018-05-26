package io.pivotal.pal.data.rentaltruck.fleet.command

import io.pivotal.pal.data.rentaltruck.fleet.domain.Truck
import io.pivotal.pal.data.rentaltruck.fleet.domain.TruckIdFactory
import io.pivotal.pal.data.rentaltruck.fleet.domain.TruckRepository
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.net.URI
import java.util.*

internal data class BuyTruckCommandDto(
    val truckName: String,
    val mileage: Int
)

class BuyTruckCommandHandler(
    private val truckRepository: TruckRepository,
    private val truckIdFactory: TruckIdFactory
) {

    fun handle(req: ServerRequest): Mono<ServerResponse> {

        return req
            .bodyToMono(BuyTruckCommandDto::class.java)
            .map { Truck.buyTruck(truckIdFactory, it.truckName, it.mileage) }
            .map { truckRepository.save(it) }
            .flatMap {
                ServerResponse
                    .created(URI.create("some-location"))
                    .build()
            }
    }
}

object RandomTruckIdFactory : TruckIdFactory {
    override fun makeId(): UUID = UUID.randomUUID()
}
