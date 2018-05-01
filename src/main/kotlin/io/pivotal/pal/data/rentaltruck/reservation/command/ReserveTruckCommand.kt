package io.pivotal.pal.data.rentaltruck.reservation.command

import io.pivotal.pal.data.framework.event.AsyncEventPublisher
import io.pivotal.pal.data.rentaltruck.event.ReservationRequestedEvent
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.net.URI
import java.time.LocalDate

data class ReserveTruckCommandDto(
        private val pickupDate: LocalDate,
        private val dropoffDate: LocalDate,
        private val customerName: String
)

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

@Service
class ReserveTruckCommandService(val eventPublisher: AsyncEventPublisher<ReservationRequestedEvent>) {

    fun reserveTruck(): Mono<String> {

        // generate confirmation number

        // emit reservation requested event
        val event = ReservationRequestedEvent(
                LocalDate.of(2018, 1, 1),
                LocalDate.of(2018, 2, 1),
                "stubbed-customer-name",
                "stubbed-confirmation-number"
        )
        eventPublisher.publish(event)

        return Mono.just("stubbed-confirmation-number")
    }

}
