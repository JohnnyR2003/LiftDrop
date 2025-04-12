package liftdrop.repository

import pt.isel.liftdrop.User
import pt.isel.liftdrop.UserRole

interface UserRepository {
    fun createUser(
        email: String,
        password: String,
        name: String,
        role: UserRole,
    ): Int

    fun deleteUser(email: String): Int

    fun findUserByName(name: String): User?

    fun findUserByEmail(email: String): User?
}
