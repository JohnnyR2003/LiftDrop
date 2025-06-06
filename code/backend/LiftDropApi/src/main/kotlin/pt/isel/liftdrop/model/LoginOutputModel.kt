package pt.isel.liftdrop.model

data class LoginOutputModel(
    val id: Int,
    val username: String,
    val email: String,
    val bearer: String,
)
