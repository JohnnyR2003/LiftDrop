package pt.isel.liftdrop.home.model.dto

data class DeliverOrderInputModel(
    val requestId: Int,
    val courierId: Int,
    val dropoffCode: String,
    val deliveryEarnings: Double
)

