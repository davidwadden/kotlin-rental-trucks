package io.pivotal.pal.data.rentaltruck.reservation.domain

import io.pivotal.pal.data.rentaltruck.KotlinRentalTrucksApplication
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("postgresql")
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [(KotlinRentalTrucksApplication::class)])
internal class ReservationRepositoryTest(@Autowired private val repository: ReservationRepository) {

    @Test
    fun `saves the reservation`() {
        val reservation = Reservation(
                "some-confirmation-number",
                "REQUESTED",
                LocalDate.of(2018, 1, 1),
                LocalDate.of(2018, 2, 1),
                "some-customer-name"
        )

        val savedReservation = repository.save(reservation)
    }

    @Test
    fun `finds the reservation`() {
        val reservation = Reservation(
                "some-confirmation-number",
                "REQUESTED",
                LocalDate.of(2018, 1, 1),
                LocalDate.of(2018, 2, 1),
                "some-customer-name"
        )
        repository.save(reservation)

        val savedReservation = repository.findById("some-confirmation-number")

        // assert savedReservation.status = FINALIZED
    }
}
