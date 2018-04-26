package io.pivotal.pal.data.rentaltruck.reservation.domain

import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.verbs.assert.assert
import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ReservationManagerTest {

    private val mockRepository: ReservationRepository = mock()
    private val reservationManager: ReservationManager = ReservationManager(mockRepository)

    @BeforeEach
    fun setUp() {
        reset(mockRepository)
    }

    @Test
    fun createReservation() {
        val reservationToReturn = Reservation(
                "some-confirmation-number",
                "REQUESTED",
                LocalDate.of(2018, 1, 1),
                LocalDate.of(2018, 2, 1),
                "some-customer-name"
        )
        whenever(mockRepository.save<Reservation>(any())).thenReturn(reservationToReturn)

        val request = ReservationRequest(
                pickupDate = LocalDate.of(2018, 1, 1),
                dropoffDate = LocalDate.of(2018, 2, 1),
                customerName = "some-customer-name",
                confirmationNumber = "some-confirmation-number"
        )

        val reservation = reservationManager.createReservation(request)
        assert(reservation).toBe(reservationToReturn)

        verify(mockRepository).save<Reservation>(any())
    }

    @Test
    fun finalizeConfirmation() {
        val mockReservation: Reservation = mock()

        reservationManager.finalizeConfirmation(mockReservation)

        inOrder(mockReservation, mockRepository) {
            verify(mockReservation).finalizeConfirmation()
            verify(mockRepository).save(mockReservation)
        }
    }
}
