package io.pivotal.pal.data.rentaltruck.reservation.command

import io.pivotal.pal.data.framework.event.AsyncEventPublisher
import io.pivotal.pal.data.rentaltruck.event.ReservationRequestedEvent
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDate

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
