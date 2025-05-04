package pt.isel.liftdrop.model

import pt.isel.liftdrop.LocationDTO

data class LocationUpdateInputModel(
    val courierId: Int,
    val newLocation: LocationDTO,
)
