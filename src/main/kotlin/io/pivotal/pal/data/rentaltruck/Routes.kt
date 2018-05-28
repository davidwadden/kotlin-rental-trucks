package io.pivotal.pal.data.rentaltruck

import io.pivotal.pal.data.rentaltruck.fleet.command.BuyTruckCommandHandler
import io.pivotal.pal.data.rentaltruck.fleet.command.InspectTruckCommandHandler
import io.pivotal.pal.data.rentaltruck.fleet.query.ListTrucksQueryHandler
import io.pivotal.pal.data.rentaltruck.rental.command.PickUpRentalCommandHandler
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.router

class Routes(
    private val buyTruckCommandHandler: BuyTruckCommandHandler,
    private val inspectTruckCommandHandler: InspectTruckCommandHandler,
    private val listTrucksQueryHandler: ListTrucksQueryHandler,
    private val pickUpRentalCommandHandler: PickUpRentalCommandHandler
) {

    fun router() = router {
        "/api".nest {
            GET("/fleet/trucks", listTrucksQueryHandler::handle)

            accept(APPLICATION_JSON).nest {
                POST("/fleet/trucks", buyTruckCommandHandler::handle)
                POST("/fleet/trucks/{truckId}/inspections", inspectTruckCommandHandler::handle)

                POST("/rental/rentals", pickUpRentalCommandHandler::handle)
            }
        }
    }
}
