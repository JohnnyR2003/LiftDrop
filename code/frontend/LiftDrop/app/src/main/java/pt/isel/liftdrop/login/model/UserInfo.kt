package pt.isel.liftdrop.login.model

data class UserInfo(
    val courierId: Int,
    val username: String,
    val email: String,
    val bearer: String,
)

