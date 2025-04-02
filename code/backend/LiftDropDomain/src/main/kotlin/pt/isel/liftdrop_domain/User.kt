package pt.isel.liftdrop_domain

sealed class User(
    val id: Long,
    val email: String,
    val password: String,
    val role: UserRole,
)

class Client(
    id: Long,
    email: String,
    password: String,
    val name: String,
) : User(id, email, password, UserRole.CLIENT)

class Courier(
    id: Long,
    email: String,
    password: String,
    val name: String,
    val currentLocation: Location,
    val isAvailable: Boolean,
) : User(id, email, password, UserRole.COURIER)
