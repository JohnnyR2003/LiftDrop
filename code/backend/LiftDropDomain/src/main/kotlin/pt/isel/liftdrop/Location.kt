package pt.isel.liftdrop

import pt.isel.pipeline.pt.isel.liftdrop.Address

data class Location(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val address: Address,
    val name: String,
)

data class PickupSpot(
    val locationId: Int,
)

class DropoffSpot(
    locationId: Int,
    clientId: Int,
)
