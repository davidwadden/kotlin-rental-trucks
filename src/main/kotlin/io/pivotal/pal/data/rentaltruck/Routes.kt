package io.pivotal.pal.data.rentaltruck

import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.router

class Routes {

    fun router() = router {
        "/api".nest {
            accept(APPLICATION_JSON).nest {
                // no-op
            }
        }
    }
}
