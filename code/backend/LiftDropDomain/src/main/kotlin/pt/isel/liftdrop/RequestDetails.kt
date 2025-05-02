package pt.isel.pipeline.pt.isel.liftdrop

data class RequestDetails(
    val restaurantName: String?,
    val description: String,
    val pickupLocation: Int,
    val dropoffLocation: Int,
)
