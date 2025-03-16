package pt.isel.pipeline

data class Courier(
    val id: Long,
    val name: String,
    val currentLocation: Location,
    val isAvailable: Boolean
)