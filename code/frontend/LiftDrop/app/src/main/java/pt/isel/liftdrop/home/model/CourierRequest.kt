package pt.isel.liftdrop.home.model

data class CourierRequest(
    val id: String,
    val pickup: String,
    val dropoff: String,
    val price: String
)