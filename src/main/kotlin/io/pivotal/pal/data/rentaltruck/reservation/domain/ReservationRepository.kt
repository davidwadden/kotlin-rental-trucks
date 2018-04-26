package io.pivotal.pal.data.rentaltruck.reservation.domain

import org.springframework.data.repository.CrudRepository

interface ReservationRepository : CrudRepository<Reservation, String>
