package pt.isel.liftdrop

data class RequestDetailsDTO(
    val requestId: Int,
    val pickupLocation: LocationDTO,
    val dropoffLocation: LocationDTO,
    val description: String,
)
