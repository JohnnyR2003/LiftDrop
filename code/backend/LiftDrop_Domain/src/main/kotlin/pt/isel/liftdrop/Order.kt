package pt.isel.liftdrop

data class Order(
    val id: Long,
    val clientId: Long,
    val courierId: Long?,
    val pickupLocation: Location,
    val deliveryLocation: Location,
    val status: OrderStatus,
    val orderETA: Long,
)
