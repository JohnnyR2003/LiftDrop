package pt.isel.liftdrop

/**
 *  Represents an authenticated user.
 *  @property user The user.
 *  @property token The authentication token.
 * */

data class AuthenticatedUser(
    val user: User,
    val token: String,
)
