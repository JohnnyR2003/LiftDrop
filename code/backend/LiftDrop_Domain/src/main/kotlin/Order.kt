package pt.isel.pipeline

data class Order(
    val id: Long,
    val clientId: Long,
    val courierId: Long?,
    val pickupLocation: Location,
    val dropoffLocation: Location,
    val status: OrderStatus,
)