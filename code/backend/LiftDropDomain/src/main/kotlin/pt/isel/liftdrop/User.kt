package pt.isel.liftdrop

data class User(
    val id: Int,
    val email: String,
    val password: String,
    val name: String,
    val role: UserRole,
)

data class Client(
    val user: User,
    val address: Int? = null,
)

data class Courier(
    val user: User,
    val currentLocation: Int?,
    val isAvailable: Boolean,
)
