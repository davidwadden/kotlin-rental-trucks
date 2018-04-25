package io.pivotal.pal.data.rentaltruck.config

import io.pivotal.pal.data.framework.event.AsyncEventPublisher
import io.pivotal.pal.data.framework.event.DefaultAsyncEventPublisher
import io.pivotal.pal.data.rentaltruck.event.ReservationRequestedEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class EventPublisherConfig {

    @Bean
    fun reservationRequestedEventPublisher(): AsyncEventPublisher<ReservationRequestedEvent> {
        return DefaultAsyncEventPublisher("reservation-requested")
    }
}
