package io.pivotal.pal.data.rentaltruck.rental.domain

import ch.tutteli.atrium.api.cc.en_UK.isNotNull
import ch.tutteli.atrium.api.cc.en_UK.isNull
import ch.tutteli.atrium.api.cc.en_UK.property
import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.verbs.assert.assert
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RentalTest {

    @Test fun `should initialize rental when picked up`() {
        val rentalId: UUID = UUID.randomUUID()
        val truckId = UUID.randomUUID()

        val mockRentalIdFactory = mock<RentalIdFactory> {
            on { makeId() } doReturn rentalId
        }

        val rental = Rental.pickUpRental(
            rentalIdFactory = mockRentalIdFactory,
            truckId = truckId,
            pickUpDate = LocalDate.of(2018, 1, 1),
            customerName = "some-customer-name"
        )

        assert(rental) {
            property(subject::id).isNotNull { toBe(rentalId) }
            property(subject::truckId).isNotNull { toBe(truckId) }
            property(subject::pickUpDate).isNotNull { toBe(LocalDate.of(2018, 1, 1)) }
            property(subject::dropOffDateTime).isNull()
            property(subject::customerName).isNotNull { toBe("some-customer-name") }
        }
    }

    @DisplayName("when the rental is dropped off")
    @Nested
    inner class DropOffTests {

        lateinit var rentalId: UUID
        lateinit var truckId: UUID
        lateinit var rental: Rental

        @BeforeEach fun setUp() {
            rentalId = UUID.randomUUID()
            truckId = UUID.randomUUID()

            val mockRentalIdFactory = mock<RentalIdFactory> {
                on { makeId() } doReturn rentalId
            }

            rental = Rental.pickUpRental(
                rentalIdFactory = mockRentalIdFactory,
                truckId = truckId,
                pickUpDate = LocalDate.of(2018, 1, 1),
                customerName = "some-customer-name"
            )
        }

        @Test fun `updates the rental status`() {
            val dropOffDateTime: LocalDateTime =
                LocalDateTime.of(2018, 2, 1, 13, 31, 24)

            rental.dropOffRental(truckId, dropOffDateTime)

            assert(rental) {
                property(subject::id).isNotNull { toBe(rentalId) }
                property(subject::dropOffDateTime).isNotNull { toBe(dropOffDateTime) }
            }
        }
    }
}
