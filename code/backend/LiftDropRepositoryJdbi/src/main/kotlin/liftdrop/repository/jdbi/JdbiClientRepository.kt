package liftdrop.repository.jdbi

import liftdrop.repository.ClientRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.liftdrop.Client

class JdbiClientRepository(
    private val handle: Handle,
) : ClientRepository {
    override fun createClient( // TODO: think about redundancy of user attributes
        userId: Int,
        address: String,
    ): Int =
        handle
            .createUpdate(
                """
                INSERT INTO client (user_id, address)
                VALUES (:userId, :address)
                """,
            ).bind("userId", userId)
            .bind("address", address)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

    override fun loginClient(
        email: String,
        password: String,
    ): Int? =
        handle
            .createQuery(
                """
                SELECT id FROM client
                WHERE email = :email AND password = :password AND is_client = true
                """,
            ).bind("email", email)
            .bind("password", password)
            .mapTo<Int>()
            .singleOrNull()

    override fun getClientByUserId(userId: Int): Client? =
        handle
            .createQuery(
                """
                SELECT * FROM client
                WHERE user_id = :userId
                """,
            ).bind("userId", userId)
            .mapTo<Client>()
            .singleOrNull()
}
