package pt.isel.liftdrop



data class Location(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val address: Address?,
    val name: String,
)

data class PickupSpot(
    val locationId: Int,
)

class DropoffSpot(
    locationId: Int,
    clientId: Int,
)
