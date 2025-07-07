package pt.isel.liftdrop.home.model.dto

data class LocationUpdateInputModel(
    val courierId : Int,
    val newLocation: LocationDTO,
)