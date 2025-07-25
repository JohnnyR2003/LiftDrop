package pt.isel.liftdrop

data class CourierWithLocation(
    val courierId: Int,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Double,
    val rating: Double?,
    val estimatedTravelTime: Long? = null,
)
