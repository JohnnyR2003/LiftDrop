package pt.isel.liftdrop_domain

data class Order(
    val id: Long,
    val clientId: Long,
    val courierId: Long?,
    val pickupLocation: Location,
    val deliveryLocation: Location,
    val status: OrderStatus,
    val orderETA: Long,
)
