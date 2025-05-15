package liftdrop.repository

import pt.isel.liftdrop.Client
import pt.isel.liftdrop.Courier
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

    fun findUserById(id: Int): User?

    fun findClientByToken(token: String): Client?

    fun findCourierByToken(token: String): Courier?

    fun getCourierIdByToken(token: String): Int?

    fun clear()
}
