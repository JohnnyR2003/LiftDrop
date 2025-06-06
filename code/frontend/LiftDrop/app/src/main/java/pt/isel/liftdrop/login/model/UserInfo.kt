package pt.isel.liftdrop.login.model

data class UserInfo(
    val id: Int,
    val username: String,
    val email: String,
    val bearer: String,
)

