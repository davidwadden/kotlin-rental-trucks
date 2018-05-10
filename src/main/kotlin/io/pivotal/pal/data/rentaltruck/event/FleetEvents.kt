package io.pivotal.pal.data.rentaltruck.event

data class TruckPurchased(
        val truckId: String,
        val mileage: Int,
        val make: String,
        val model: String
)
