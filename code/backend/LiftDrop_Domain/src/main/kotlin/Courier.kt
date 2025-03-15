package pt.isel.pipeline

data class Courier(
    val id: Long,
    val currentLocation: Location,
    val isAvailable: Boolean
)