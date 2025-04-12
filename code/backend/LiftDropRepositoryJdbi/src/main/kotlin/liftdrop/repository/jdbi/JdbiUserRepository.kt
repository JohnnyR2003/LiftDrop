package liftdrop.repository.jdbi

import liftdrop.repository.UserRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.liftdrop.User
import pt.isel.liftdrop.UserRole

class JdbiUserRepository(
    private val handle: Handle,
) : UserRepository {
    override fun createUser(
        email: String,
        password: String,
        name: String,
        role: UserRole,
    ): Int { // check first if there's already a user with that email and or name
        val existingUser = findUserByEmail(email)
        if (existingUser != null && existingUser.email == email) {
            return 0
        }
        return handle
            .createUpdate(
                """
            INSERT INTO liftdrop.user (email, password, name, role)
            VALUES (:email, :password, :name, :role)
            """,
            ).bind("email", email)
            .bind("password", password)
            .bind("name", name)
            .bind("role", role.name)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()
    }

    override fun deleteUser(email: String): Int {
        val user = findUserByEmail(email) ?: return 0
        return handle
            .createUpdate(
                """
            DELETE FROM liftdrop.user
            WHERE email = :email
            """,
            ).bind("email", email)
            .execute()
    }

    override fun findUserByName(name: String): User? =
        handle
            .createQuery(
                """
            SELECT * FROM liftdrop.user
            WHERE name = :name
            """,
            ).bind("name", name)
            .mapTo<User>()
            .singleOrNull()

    override fun findUserByEmail(email: String): User? =
        handle
            .createQuery(
                """
            SELECT * FROM liftdrop.user
            WHERE email = :email
            """,
            ).bind("email", email)
            .mapTo<User>()
            .singleOrNull()

    override fun findUserById(id: Int): User? =
        handle
            .createQuery(
                """
            SELECT * FROM liftdrop.user
            WHERE user_id = :id
            """,
            ).bind("id", id)
            .mapTo<User>()
            .singleOrNull()
}
