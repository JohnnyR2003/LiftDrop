package pt.isel.liftdrop.home.model.dto

data class PickupOrderInputModel(
    val requestId: Int,
    val courierId: Int,
    val pickupCode: String,
    val deliveryKind: String
)
