package pt.isel.liftdrop.home.model

data class LocationUpdateInputModel(
    val courierId : Int,
    val newLocation: LocationDTO,
)