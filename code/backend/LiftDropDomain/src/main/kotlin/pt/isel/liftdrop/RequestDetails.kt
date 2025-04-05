package pt.isel.pipeline.pt.isel.liftdrop

import pt.isel.liftdrop.Location

data class RequestDetails(
    val restaurantName: String,
    val description: String,
    val pickupLocation: Location,
    val dropOffLocation: Location,
)
