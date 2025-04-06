package liftdrop.repository

import pt.isel.liftdrop.User

interface UserRepository {
    fun createUser(
        email: String,
        password: String,
        name: String,
    ): Int

    fun findUserByEmail(email: String): User?
}
