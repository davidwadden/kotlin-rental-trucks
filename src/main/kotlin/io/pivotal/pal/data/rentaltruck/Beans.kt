package io.pivotal.pal.data.rentaltruck

import io.pivotal.pal.data.framework.event.AsyncEventHandler
import io.pivotal.pal.data.framework.event.AsyncEventPublisher
import io.pivotal.pal.data.framework.event.AsyncEventSubscriberAdapter
import io.pivotal.pal.data.framework.event.DefaultAsyncEventPublisher
import io.pivotal.pal.data.rentaltruck.event.EventType
import io.pivotal.pal.data.rentaltruck.fleet.handler.EventTypeEventHandler
import io.pivotal.pal.data.rentaltruck.reservation.command.CreateRentalCommandHandler
import io.pivotal.pal.data.rentaltruck.reservation.command.CreateReservationCommandHandler
import io.pivotal.pal.data.rentaltruck.reservation.command.DropOffRentalCommandHandler
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans

fun beans() = beans {
    bean<CreateReservationCommandHandler>()
    bean<CreateRentalCommandHandler>()
    bean<DropOffRentalCommandHandler>()

    bean {
        Routes(ref(), ref(), ref()).router()
    }

    bean<AsyncEventPublisher<EventType>> {
        DefaultAsyncEventPublisher("event-type")
    }
    bean<AsyncEventHandler<EventType>> {
        EventTypeEventHandler()
    }
    bean<AsyncEventSubscriberAdapter<EventType>> {
        AsyncEventSubscriberAdapter("event-type", ref())
    }

}

// See application.yml context.initializer.classes entry
@Suppress("unused")
class BeansInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(context: GenericApplicationContext) =
            beans().initialize(context)

}
