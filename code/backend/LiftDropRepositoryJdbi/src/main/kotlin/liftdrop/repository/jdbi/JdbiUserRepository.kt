package liftdrop.repository.jdbi

import liftdrop.repository.UserRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.liftdrop.User

class JdbiUserRepository(
    private val handle: Handle,
) : UserRepository {
    override fun createUser(
        email: String,
        password: String,
        name: String,
    ): Int =
        handle
            .createUpdate(
                """
            INSERT INTO users (email, password, name)
            VALUES (:email, :password, :name)
            """,
            ).bind("email", email)
            .bind("password", password)
            .bind("name", name)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

    override fun findUserByEmail(email: String): User? =
        handle
            .createQuery(
                """
            SELECT * FROM users
            WHERE email = :email
            """,
            ).bind("email", email)
            .mapTo<User>()
            .singleOrNull()
}
