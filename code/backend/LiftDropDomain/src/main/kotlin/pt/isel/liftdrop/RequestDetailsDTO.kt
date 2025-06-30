package pt.isel.liftdrop

data class RequestDetailsDTO(
    val requestId: Int,
    val pickupLocation: LocationDTO,
    val pickupAddress: String,
    val dropoffLocation: LocationDTO,
    val dropoffAddress: String,
    val item: String,
    val quantity: Int,
    val price: String,
)
