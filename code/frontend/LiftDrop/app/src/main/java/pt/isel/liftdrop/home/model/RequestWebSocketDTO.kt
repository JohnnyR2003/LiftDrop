package pt.isel.liftdrop.home.model

data class RequestWebSocketDTO(
    val id: Int,
    val lat: Double,
    val lon: Double,
    val userId: Int
)