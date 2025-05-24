package pt.isel.liftdrop.home.model

import kotlinx.serialization.Serializable

data class CourierRequest(
    val id: String,
    val pickup: String,
    val dropoff: String,
    val price: String
)

@Serializable
data class CourierRequestDetails(
    val courierId : String,
    val requestId: String,
    val pickupLatitude: Double,
    val pickupLongitude: Double,
    val pickupAddress: String,
    val dropoffLatitude: Double,
    val dropoffLongitude: Double,
    val dropoffAddress: String,
    val price: String
)
