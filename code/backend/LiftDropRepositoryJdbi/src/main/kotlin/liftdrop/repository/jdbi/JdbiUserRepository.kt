package liftdrop.repository.jdbi

import liftdrop.repository.UserRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.liftdrop.User
import pt.isel.liftdrop.UserRole

class JdbiUserRepository(
    private val handle: Handle,
) : UserRepository {
    /**
     * Creates a new user in the database.
     *
     * @param email The email of the user to create.
     * @param password The password of the user to create.
     * @param name The name of the user to create.
     * @param role The role of the user to create.
     * @return The ID of the created user, or 0 if a user with the same email already exists.
     */
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

    /**
     * Deletes a user from the database(Admin only).
     *
     * @param email The email of the user to delete.
     * @return The number of rows affected by the delete operation.
     */
    override fun deleteUser(email: String): Int {
        findUserByEmail(email) ?: return 0
        return handle
            .createUpdate(
                """
            DELETE FROM liftdrop.user
            WHERE email = :email
            """,
            ).bind("email", email)
            .execute()
    }

    /**
     * Gets a user by their name.
     *
     * @param name The name of the user to get.
     * @return The user with the given name, or null if no such user exists.
     */
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

    /**
     * Gets a user by their email.
     *
     * @param email The email of the user to get.
     * @return The user with the given email, or null if no such user exists.
     */
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

    /**
     * Gets a user by their ID.
     *
     * @param id The ID of the user to get.
     * @return The user with the given ID, or null if no such user exists.
     */
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
