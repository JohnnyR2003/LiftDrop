package pt.isel.liftdrop

data class RequestDetailsDTO(
    val requestId: Int,
    val pickupLocation: LocationDTO,
    val dropoffSpot: LocationDTO,
    val description: String,
)
