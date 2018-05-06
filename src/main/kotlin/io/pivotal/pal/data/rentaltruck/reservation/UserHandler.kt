package io.pivotal.pal.data.rentaltruck.reservation

import io.pivotal.pal.data.rentaltruck.formatDate
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToServerSentEvents
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDate

data class User(val firstName: String, val lastName: String, val birthDate: LocalDate)

class UserHandler {

    private val users = Flux.just(
            User("Foo", "Foo", LocalDate.now().minusDays(1)),
            User("Bar", "Bar", LocalDate.now().minusDays(10)),
            User("Baz", "Baz", LocalDate.now().minusDays(100)))

    private val userStream = Flux
            .zip(Flux.interval(Duration.ofMillis(100)), users.repeat())
            .map { it.t2 }

    fun findAll(req: ServerRequest): Mono<ServerResponse> =
            ok().body(users.map { it.toDto() })

    fun stream(req: ServerRequest): Mono<ServerResponse> =
            ok().bodyToServerSentEvents(userStream.map { it.toDto() })
}

class UserDto(val firstName: String, val lastName: String, val birthDate: String)

fun User.toDto() = UserDto(firstName, lastName, birthDate.formatDate())
