package io.pivotal.pal.data.rentaltruck

import io.pivotal.pal.data.rentaltruck.reservation.Routes
import io.pivotal.pal.data.rentaltruck.reservation.UserHandler
import io.pivotal.pal.data.rentaltruck.reservation.command.CreateRentalCommandHandler
import io.pivotal.pal.data.rentaltruck.reservation.command.CreateReservationCommandHandler
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans

fun beans() = beans {
    bean<UserHandler>()
    bean<CreateReservationCommandHandler>()
    bean<CreateRentalCommandHandler>()

    bean {
        Routes(ref(), ref(), ref()).router()
    }
}

// See application.yml context.initializer.classes entry
@Suppress("unused")
class BeansInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(context: GenericApplicationContext) =
            beans().initialize(context)

}
