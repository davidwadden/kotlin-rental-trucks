package io.pivotal.pal.data.rentaltruck.config

import io.pivotal.pal.data.framework.event.AsyncEventSubscriberAdapter
import io.pivotal.pal.data.rentaltruck.event.ReservationRequestedEvent
import io.pivotal.pal.data.rentaltruck.reservation.handler.SaveReservationEventSubscriber
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SubscriberAdapterConfig {

    @Bean
    fun saveReservationSubscriberAdapter(subscriber: SaveReservationEventSubscriber): AsyncEventSubscriberAdapter<ReservationRequestedEvent> {
        return AsyncEventSubscriberAdapter("reservation-requested", subscriber)
    }
}
