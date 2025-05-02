package pt.isel.liftdrop

import pt.isel.pipeline.pt.isel.liftdrop.LocationDTO

data class RequestDetailsDTO(
    val requestId: Int,
    val pickupLocation: LocationDTO,
    val dropoffSpot: LocationDTO,
    val description: String,
)
