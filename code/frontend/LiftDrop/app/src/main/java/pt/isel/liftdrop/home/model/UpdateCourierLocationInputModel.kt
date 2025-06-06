package pt.isel.liftdrop.home.model

data class UpdateCourierLocationInputModel(
    val courierId : Int,
    val latitude: Double,
    val longitude: Double
)