package pt.isel.services.courier

data class UserDetails(
    val courierId: Int,
    val username: String,
    val email: String,
    val token: String,
)
