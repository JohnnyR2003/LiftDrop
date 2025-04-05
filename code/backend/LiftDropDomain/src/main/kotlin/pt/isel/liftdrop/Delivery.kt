package pt.isel.liftdrop

data class Delivery(
    val id: Long,
    val startedAt: Long,
    val status: RequestStatus,
    val orderETA: Long,
)
