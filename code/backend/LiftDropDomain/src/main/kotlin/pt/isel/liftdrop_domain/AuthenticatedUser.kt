package pt.isel.liftdrop_domain

/**
 *  Represents an authenticated user.
 *  @property user The user.
 *  @property token The authentication token.
 * */

class AuthenticatedUser(
    val user: User,
    val token: String,
)
