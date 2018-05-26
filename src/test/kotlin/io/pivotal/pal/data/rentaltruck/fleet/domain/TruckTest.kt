package io.pivotal.pal.data.rentaltruck.fleet.domain

import ch.tutteli.atrium.api.cc.en_UK.isNotNull
import ch.tutteli.atrium.api.cc.en_UK.property
import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.verbs.assert.assert
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TruckTest {

    @Test fun `generates the truckId when the truck is bought`() {
        val truckId: UUID = UUID.randomUUID()

        val mockTruckIdFactory = mock<TruckIdFactory> {
            on { makeId() } doReturn truckId
        }

        val truck = Truck.buyTruck(mockTruckIdFactory, "some-truck-name", 10000)

        assert(truck) {
            property(subject::truckId).isNotNull { toBe(truckId) }
            property(subject::truckName).isNotNull { toBe("some-truck-name") }
            property(subject::status).isNotNull { toBe(TruckStatus.MAINTENANCE) }
            property(subject::mileage).isNotNull { toBe(10000) }
        }
    }
}
