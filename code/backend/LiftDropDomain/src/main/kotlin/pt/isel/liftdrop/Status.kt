package pt.isel.liftdrop

data class RequestStatus(
    val status: Status,
    val orderETA: Long?,
)

enum class Status {
    PENDING,
    ACCEPTED,
    PICKING_UP,
    PICKED_UP,
    DELIVERING,
    DELIVERED,
    CANCELLED,
}

fun String.parseStatus(): Status? {
    when (this.uppercase()) {
        "PENDING" -> return Status.PENDING
        "ACCEPTED" -> return Status.ACCEPTED
        "PICKING_UP" -> return Status.PICKING_UP
        "PICKED_UP" -> return Status.PICKED_UP
        "DELIVERING" -> return Status.DELIVERING
        "DELIVERED" -> return Status.DELIVERED
        "CANCELLED" -> return Status.CANCELLED
    }
    return null
}
