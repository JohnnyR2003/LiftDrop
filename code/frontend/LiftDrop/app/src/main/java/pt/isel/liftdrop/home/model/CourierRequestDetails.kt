package pt.isel.liftdrop.home.model

import kotlinx.serialization.Serializable

@Serializable
data class CourierRequestDetails(
    val type: String = "DELIVERY_REQUEST",
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
