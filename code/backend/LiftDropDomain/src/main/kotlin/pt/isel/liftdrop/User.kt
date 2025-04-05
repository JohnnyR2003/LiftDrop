package pt.isel.liftdrop

sealed class User(
    val id: Long,
    val email: String,
    val password: String,
    val name: String,
    val role: UserRole,
)

class Client(
    id: Long,
    email: String,
    password: String,
    name: String,
    val address: Location,
) : User(id, email, password, name, UserRole.CLIENT)

class Courier(
    id: Long,
    email: String,
    password: String,
    name: String,
    val currentLocation: Location,
    val isAvailable: Boolean,
) : User(id, email, password, name, UserRole.COURIER)
