package pt.isel.liftdrop

sealed class Location(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val address: String,
)

class PickupSpot(
    id: Long,
    latitude: Double,
    longitude: Double,
    address: String,
    val name: String,
) : Location(id, latitude, longitude, address)

class DropoffSpot(
    id: Long,
    latitude: Double,
    longitude: Double,
    address: String,
) : Location(id, latitude, longitude, address)
