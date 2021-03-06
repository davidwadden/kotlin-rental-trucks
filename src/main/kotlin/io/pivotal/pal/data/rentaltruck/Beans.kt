package io.pivotal.pal.data.rentaltruck

import io.pivotal.pal.data.framework.store.EventStoreRepositoryAdapter
import io.pivotal.pal.data.rentaltruck.fleet.command.BuyTruckCommandHandler
import io.pivotal.pal.data.rentaltruck.fleet.command.InspectTruckCommandHandler
import io.pivotal.pal.data.rentaltruck.fleet.command.RandomTruckIdFactory
import io.pivotal.pal.data.rentaltruck.fleet.domain.TruckRepository
import io.pivotal.pal.data.rentaltruck.fleet.query.ListTrucksQueryHandler
import io.pivotal.pal.data.rentaltruck.rental.command.PickUpRentalCommandHandler
import io.pivotal.pal.data.rentaltruck.rental.command.RandomRentalIdFactory
import io.pivotal.pal.data.rentaltruck.rental.domain.RentalRepository
import io.pivotal.pal.data.rentaltruck.rental.domain.RentalTruckRepository
import io.pivotal.pal.data.rentaltruck.rental.query.ListRentalsQueryHandler
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans

fun beans() = beans {
    bean<RandomTruckIdFactory>()

    bean<BuyTruckCommandHandler>()
    bean<InspectTruckCommandHandler>()

    bean<ListTrucksQueryHandler>()

    bean<TruckRepository> {
        EventStoreRepositoryAdapter(ref(), ref())
    }


    bean<RandomRentalIdFactory>()

    bean<PickUpRentalCommandHandler>()

    bean<ListRentalsQueryHandler>()

    bean<RentalRepository> {
        EventStoreRepositoryAdapter(ref(), ref())
    }
    bean<RentalTruckRepository> {
        EventStoreRepositoryAdapter(ref(), ref())
    }


    bean {
        Routes(ref(), ref(), ref(), ref(), ref()).router()
    }
}

// See application.yml context.initializer.classes entry
@Suppress("unused")
class BeansInitializer : ApplicationContextInitializer<GenericApplicationContext> {

    override fun initialize(context: GenericApplicationContext) =
        beans().initialize(context)
}
