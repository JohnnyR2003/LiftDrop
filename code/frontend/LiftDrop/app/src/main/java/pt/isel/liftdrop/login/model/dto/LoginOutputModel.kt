package pt.isel.liftdrop.login.model.dto

data class LoginOutputModel(
    val id: Int,
    val username: String,
    val email: String,
    val token: String)
