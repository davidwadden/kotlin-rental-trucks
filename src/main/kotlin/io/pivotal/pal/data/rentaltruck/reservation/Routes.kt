package io.pivotal.pal.data.rentaltruck.reservation

import io.pivotal.pal.data.rentaltruck.reservation.command.CreateRentalCommandHandler
import io.pivotal.pal.data.rentaltruck.reservation.command.CreateReservationCommandHandler
import io.pivotal.pal.data.rentaltruck.reservation.command.DropOffRentalCommandHandler
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.router

class Routes(
        private val createReservationCommandHandler: CreateReservationCommandHandler,
        private val createRentalCommandHandler: CreateRentalCommandHandler,
        private val dropOffRentalCommandHandler: DropOffRentalCommandHandler
) {

    fun router() = router {
        "/api".nest {
            accept(APPLICATION_JSON).nest {
                POST("/reservations", createReservationCommandHandler::create)
                POST("/rentals", createRentalCommandHandler::create)
                POST("/rentals/drop-offs", dropOffRentalCommandHandler::create)
            }
        }
    }
}
