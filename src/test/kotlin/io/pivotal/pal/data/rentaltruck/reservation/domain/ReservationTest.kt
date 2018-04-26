package io.pivotal.pal.data.rentaltruck.reservation.domain

import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.verbs.assert.assert
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ReservationTest {

    @Test
    fun `finalizes the confirmation`() {
        val reservation = Reservation(
                "some-confirmation-number",
                "REQUESTED",
                LocalDate.of(2018, 1, 1),
                LocalDate.of(2018, 2, 1),
                "some-customer-name"
        )

        reservation.finalizeConfirmation()
        assert(reservation.status).toBe("FINALIZED")
    }
}
