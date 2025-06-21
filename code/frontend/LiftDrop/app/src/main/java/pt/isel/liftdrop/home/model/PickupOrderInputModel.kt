package pt.isel.liftdrop.home.model

data class PickupOrderInputModel(
    val requestId: Int,
    val courierId: Int,
    val pickupCode: String,
    val deliveryKind: String
)
