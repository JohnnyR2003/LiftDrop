package pt.isel.liftdrop.model

data class RegisterUserOutput(
    val id: String,
    val name: String,
    val email: String,
    val password: String,
)
