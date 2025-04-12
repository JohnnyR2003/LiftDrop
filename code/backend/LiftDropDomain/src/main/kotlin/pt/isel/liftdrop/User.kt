package pt.isel.liftdrop

data class User(
    val id: Int,
    val email: String,
    val password: String,
    val name: String,
    val role: UserRole,
)

data class Client(
    val userId: Int,
    val address: Int? = null,
)

data class Courier(
    val userId: Int,
    val currentLocation: Int?,
    val isAvailable: Boolean,
)
