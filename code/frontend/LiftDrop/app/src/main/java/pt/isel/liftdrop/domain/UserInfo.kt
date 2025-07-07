package pt.isel.liftdrop.domain

data class UserInfo(
    val id: Int,
    val username: String,
    val email: String,
    val bearer: String,
)