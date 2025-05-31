package liftdrop.repository.jdbi

import liftdrop.repository.UserRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.liftdrop.Client
import pt.isel.liftdrop.Courier
import pt.isel.liftdrop.User
import pt.isel.liftdrop.UserRole
import java.sql.ResultSet

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
    ): Int =
        handle
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

    /**
     * Deletes a user from the database(Admin only).
     *
     * @param email The email of the user to delete.
     * @return The number of rows affected by the delete operation.
     */
    override fun deleteUser(email: String): Int =
        handle
            .createUpdate(
                """
            DELETE FROM liftdrop.user
            WHERE email = :email
            """,
            ).bind("email", email)
            .execute()

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

    override fun findClientByToken(token: String): Client? =
        handle
            .createQuery(
                """
                SELECT 
                    u.user_id, u.email, u.password, u.name, u.role,
                    c.address
                FROM liftdrop.sessions s
                JOIN liftdrop.user u ON s.user_id = u.user_id
                JOIN liftdrop.client c ON u.user_id = c.client_id
                WHERE s.session_token = :token
                """.trimIndent(),
            ).bind("token", token)
            .map { rs, _ ->
                val user = mapToUser(rs)
                val address = rs.getInt("address").takeIf { !rs.wasNull() }

                Client(user = user, address = address)
            }.singleOrNull()

    override fun findCourierByToken(token: String): Courier? =
        handle
            .createQuery(
                """
                SELECT 
                    u.user_id, u.email, u.password, u.name, u.role,
                    c.current_location, c.is_available
                FROM liftdrop.sessions s
                JOIN liftdrop.user u ON s.user_id = u.user_id
                JOIN liftdrop.courier c ON u.user_id = c.courier_id
                WHERE s.session_token = :token
                """.trimIndent(),
            ).bind("token", token)
            .map { rs, _ ->
                val user =
                    User(
                        id = rs.getInt("user_id"),
                        email = rs.getString("email"),
                        password = rs.getString("password"),
                        name = rs.getString("name"),
                        role = UserRole.valueOf(rs.getString("role")),
                    )

                val currentLocation = rs.getInt("current_location").takeIf { !rs.wasNull() }
                val isAvailable = rs.getBoolean("is_available")

                Courier(
                    user = user,
                    currentLocation = currentLocation,
                    isAvailable = isAvailable,
                )
            }.singleOrNull()

    override fun getCourierIdByToken(token: String): Int? {
        println(token)
        val user =
            handle
                .createQuery(
                    """
                    SELECT u.user_id
                    FROM liftdrop.sessions s
                    JOIN liftdrop.user u ON s.user_id = u.user_id
                    WHERE s.session_token = :token
                    """.trimIndent(),
                ).bind("token", token)
                .mapTo<Int>()
                .firstOrNull()

        println("the following courierId was fetched from the database: $user")

        return handle
            .createQuery(
                """
                SELECT u.user_id
                FROM liftdrop.sessions s
                JOIN liftdrop.user u ON s.user_id = u.user_id
                WHERE s.session_token = :token
                """.trimIndent(),
            ).bind("token", token)
            .mapTo<Int>()
            .firstOrNull()
    }

    override fun clear() {
        handle.createUpdate("TRUNCATE TABLE liftdrop.user RESTART IDENTITY CASCADE;").execute()
    }
}

private fun mapToUser(rs: ResultSet): User =
    User(
        id = rs.getInt("user_id"),
        email = rs.getString("email"),
        password = rs.getString("password"),
        name = rs.getString("name"),
        role = UserRole.valueOf(rs.getString("role")),
    )
