package pt.isel.liftdrop

data class User(
    val id: Long,
    val email: String,
    val password: String,
    val role: UserRole,
)
