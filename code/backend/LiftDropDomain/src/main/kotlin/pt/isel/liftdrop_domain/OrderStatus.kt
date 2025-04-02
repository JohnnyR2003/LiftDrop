package pt.isel.liftdrop_domain

enum class OrderStatus {
    PENDING,
    ACCEPTED,
    PICKING_UP,
    PICKED_UP,
    DELIVERING,
    DELIVERED,
    CANCELLED,
}

fun String.parseStatus(): OrderStatus? {
    when (this.uppercase()) {
        "PENDING" -> return OrderStatus.PENDING
        "ACCEPTED" -> return OrderStatus.ACCEPTED
        "PICKING_UP" -> return OrderStatus.PICKING_UP
        "PICKED_UP" -> return OrderStatus.PICKED_UP
        "DELIVERING" -> return OrderStatus.DELIVERING
        "DELIVERED" -> return OrderStatus.DELIVERED
        "CANCELLED" -> return OrderStatus.CANCELLED
    }
    return null
}
