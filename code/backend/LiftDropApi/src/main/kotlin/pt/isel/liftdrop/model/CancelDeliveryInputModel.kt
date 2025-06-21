package pt.isel.liftdrop.model

import pt.isel.liftdrop.LocationDTO

data class CancelDeliveryInputModel(
    val courierId: Int,
    val requestId: Int,
    val deliveryStatus: String,
    val pickupLocationDTO: LocationDTO?,
)
