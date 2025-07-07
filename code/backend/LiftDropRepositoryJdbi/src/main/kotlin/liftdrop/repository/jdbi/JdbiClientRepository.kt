package liftdrop.repository.jdbi

import liftdrop.repository.ClientRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.liftdrop.Address
import pt.isel.liftdrop.Client
import pt.isel.liftdrop.User
import pt.isel.liftdrop.UserRole

class JdbiClientRepository(
    private val handle: Handle,
) : ClientRepository {
    /**
     * Creates a new client in the database.
     *
     * @param clientId The ID of the client to create.
     * @param address The address of the client.
     * @return The ID of the created client.
     */
    override fun createClient(
        clientId: Int,
        address: Address,
    ): Int {
        val addressId =
            handle
                .createUpdate(
                    """
                INSERT INTO liftdrop.address (country, city, street, house_number, floor, zip_code)
                VALUES (:country, :city, :street, :house_number, :zip_code, :country)
                """,
                ).bind("country", address.country)
                .bind("city", address.city)
                .bind("street", address.street)
                .bind("house_number", address.streetNumber)
                .bind("floor", address.floor)
                .bind("zip_code", address.zipCode)
                .executeAndReturnGeneratedKeys()
                .mapTo<Int>()
                .one()

        return handle
            .createUpdate(
                """
                INSERT INTO liftdrop.client (client_id, address)
                VALUES (:clientId, :addressId)
                """,
            ).bind("clientId", clientId)
            .bind("addressId", addressId)
            .execute()
            .let { clientId }
    }

    /**
     * Logs in a client using their email and password.
     *
     * @param email The email of the client.
     * @param password The password of the client.
     * @return The ID of the authenticated client, or null if authentication fails.
     */
    override fun loginClient(
        email: String,
        password: String,
    ): Pair<Int, String>? =
        handle
            .createQuery(
                """
                SELECT u.user_id, u.password
                FROM liftdrop.user u
                WHERE u.email = :email
                """.trimIndent(),
            ).bind("email", email)
            .map { rs, _ ->
                val userId = rs.getInt("user_id")
                val userPassword = rs.getString("password")
                Pair<Int, String>(userId, userPassword)
            }.singleOrNull()

    /**
     * Gets a client by their user ID.
     *
     * @param userId The ID of the user to get.
     * @return The client with the specified user ID, or null if not found.
     */
    override fun getClientByUserId(userId: Int): Client? =
        handle
            .createQuery(
                """
                SELECT 
                    u.user_id, u.email, u.password, u.name, u.role,
                    c.address
                FROM liftdrop.user u
                JOIN liftdrop.client c ON u.user_id = c.client_id
                WHERE u.user_id = :userId
                """.trimIndent(),
            ).bind("userId", userId)
            .map { rs, _ ->
                val user =
                    User(
                        id = rs.getInt("user_id"),
                        email = rs.getString("email"),
                        password = rs.getString("password"),
                        name = rs.getString("name"),
                        role = UserRole.valueOf(rs.getString("role")),
                    )
                val address = rs.getInt("address").takeIf { !rs.wasNull() }

                Client(user = user, address = address)
            }.singleOrNull()

    override fun createClientSession(
        userId: Int,
        sessionToken: String,
    ): String? =
        handle
            .createUpdate(
                """
                INSERT INTO liftdrop.sessions (user_id, session_token, created_at, role)
                VALUES (:userId, :sessionToken, EXTRACT(EPOCH FROM NOW()), :role)
                RETURNING session_token
                """.trimIndent(),
            ).bind("userId", userId)
            .bind("sessionToken", sessionToken)
            .bind("role", UserRole.CLIENT.name)
            .executeAndReturnGeneratedKeys()
            .mapTo<String>()
            .singleOrNull()

    override fun logoutClient(sessionToken: String): Boolean {
        val rowsUpdated =
            handle
                .createUpdate(
                    """
                    DELETE FROM liftdrop.sessions
                    WHERE session_token = :sessionToken
                    """.trimIndent(),
                ).bind("sessionToken", sessionToken)
                .execute()
        return rowsUpdated > 0
    }

    override fun getRequestStatus(
        clientId: Int,
        requestId: Int,
    ): Pair<String, String>? =
        handle
            .createQuery(
                """
                SELECT 
                    r.eta AS original_eta,
                    EXTRACT(EPOCH FROM NOW()) AS "current_time",
                    GREATEST(r.eta - EXTRACT(EPOCH FROM NOW()), 0) AS current_eta, 
                    r.request_status AS status
                FROM liftdrop.request r 
                WHERE r.client_id = :clientId AND r.request_id = :requestId
                """.trimIndent(),
            ).bind("clientId", clientId)
            .bind("requestId", requestId)
            .map { rs, _ ->
                val status = rs.getString("status")
                val currentEta = rs.getInt("current_eta").toString()

                Pair(currentEta, status)
            }.singleOrNull()

    override fun clear() {
        handle.createUpdate("TRUNCATE TABLE liftdrop.client CASCADE;").execute()
    }
}
