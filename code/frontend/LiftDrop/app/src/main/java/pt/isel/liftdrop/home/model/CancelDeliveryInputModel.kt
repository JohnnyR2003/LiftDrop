package pt.isel.liftdrop.home.model

data class CancelDeliveryInputModel(
    val courierId: Int,
    val requestId: Int,
    val deliveryStatus: String,
    val pickupLocation: LocationDTO?
)