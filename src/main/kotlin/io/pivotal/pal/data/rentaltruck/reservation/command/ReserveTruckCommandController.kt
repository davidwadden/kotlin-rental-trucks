package io.pivotal.pal.data.rentaltruck.reservation.command

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class ReserveTruckCommandController(private val service: ReserveTruckCommandService) {

    @PostMapping("/reservations")
    fun reserveTruck(commandDto: ReserveTruckCommandDto): ResponseEntity<Void> {
        // obtain confirmation number by calling service
        val confirmationNumber = service.reserveTruck()

        // TODO: construct location header from confirmation number
        val locationUri = URI.create("/reservations/stubbed-confirmation-number")

        return ResponseEntity.accepted()
                .location(locationUri)
                .build()
    }
}
