package pt.isel.liftdrop.home.model.dto

data class CancelDeliveryInputModel(
    val courierId: Int,
    val requestId: Int,
    val deliveryStatus: String,
    val pickupLocation: LocationDTO?
)