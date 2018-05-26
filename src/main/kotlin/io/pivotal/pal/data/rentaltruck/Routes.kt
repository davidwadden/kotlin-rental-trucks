package io.pivotal.pal.data.rentaltruck

import io.pivotal.pal.data.rentaltruck.fleet.command.BuyTruckCommandHandler
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.router

class Routes(
    private val buyTruckCommandHandler: BuyTruckCommandHandler
) {

    fun router() = router {
        "/api".nest {
            accept(APPLICATION_JSON).nest {
                POST("/fleet/trucks", buyTruckCommandHandler::handle)
            }
        }
    }
}
