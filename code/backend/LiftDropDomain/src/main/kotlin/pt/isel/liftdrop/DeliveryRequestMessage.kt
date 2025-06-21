package pt.isel.pipeline.pt.isel.liftdrop

data class DeliveryRequestMessage(
    val type: String = "DELIVERY_REQUEST",
    val requestId: Int,
    val courierId: Int,
    val pickupLatitude: Double,
    val pickupLongitude: Double,
    val pickupAddress: String,
    val dropoffLatitude: Double,
    val dropoffLongitude: Double,
    val dropoffAddress: String,
    val price: String,
    val deliveryKind: String,
)
