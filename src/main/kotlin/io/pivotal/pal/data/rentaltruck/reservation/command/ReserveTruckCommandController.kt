package io.pivotal.pal.data.rentaltruck.reservation.command

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.net.URI

@RestController
class ReserveTruckCommandController(private val service: ReserveTruckCommandService) {

    @PostMapping("/reservations")
    fun reserveTruck(commandDto: ReserveTruckCommandDto): Mono<ResponseEntity<Void>> {

        // obtain confirmation number by calling service
        return service.reserveTruck()
                .map { _ ->
                    // TODO: construct location header from confirmation number
                    val locationUri = URI.create("/reservations/stubbed-confirmation-number")

                    return@map ResponseEntity.accepted()
                            .location(locationUri)
                            .build<Void>()
                }
    }
}
