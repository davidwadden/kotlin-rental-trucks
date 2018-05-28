package io.pivotal.pal.data.rentaltruck

import io.pivotal.pal.data.framework.store.EventStoreRepository
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EntityScan(basePackageClasses = [EventStoreRepository::class])
@EnableJpaRepositories(basePackageClasses = [EventStoreRepository::class])
@EnableJpaAuditing
@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
