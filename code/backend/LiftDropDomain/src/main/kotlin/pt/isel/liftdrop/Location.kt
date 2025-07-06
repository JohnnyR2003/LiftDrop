package pt.isel.liftdrop

data class Location(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val address: Address?,
    val name: String,
)
