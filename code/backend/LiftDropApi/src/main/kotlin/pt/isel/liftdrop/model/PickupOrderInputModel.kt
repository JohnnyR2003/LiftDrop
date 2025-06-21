package pt.isel.liftdrop.model

data class PickupOrderInputModel(
    val requestId: Int,
    val courierId: Int,
    val pickupCode: String,
    val deliveryKind: String,
)
