package pt.isel.liftdrop

data class RequestStatus(
    val status: Status,
    val orderETA: Long?,
)

enum class Status {
    PENDING,
    IN_PROGRESS,
    PICKED_UP,
    DROPPED_OFF,
    CANCELLED,
}

fun String.parseStatus(): Status? {
    when (this.uppercase()) {
        "PENDING" -> return Status.PENDING
        "IN_PROGRESS" -> return Status.IN_PROGRESS
        "PICKED_UP" -> return Status.PICKED_UP
        "DROPPED_OFF" -> return Status.DROPPED_OFF
        "CANCELLED" -> return Status.CANCELLED
    }
    return null
}
