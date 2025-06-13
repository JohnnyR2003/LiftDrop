package pt.isel.liftdrop.model

data class DeliverOrderInputModel(
    val requestId: Int,
    val courierId: Int,
    val dropoffCode: String,
)
