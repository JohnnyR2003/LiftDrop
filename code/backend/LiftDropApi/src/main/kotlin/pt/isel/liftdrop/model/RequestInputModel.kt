package pt.isel.liftdrop.model

import pt.isel.liftdrop.LocationDTO

data class RequestInputModel(
    val restaurantName: String,
    val itemDesignation: String,
    val dropOffLocation: LocationDTO,
)
