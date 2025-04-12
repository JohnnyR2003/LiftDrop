package liftdrop.repository.jdbi

import liftdrop.repository.ClientRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.liftdrop.Client
import pt.isel.pipeline.pt.isel.liftdrop.Address

class JdbiClientRepository(
    private val handle: Handle,
) : ClientRepository {
    override fun createClient(
        clientId: Int,
        address: Address,
    ): Int { // first create address then create client with the address id
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

    override fun loginClient(
        email: String,
        password: String,
    ): Int? =
        handle
            .createQuery(
                """
                SELECT user_id FROM liftdrop.user
                WHERE email = :email AND password = :password AND role = 'CLIENT'
                """,
            ).bind("email", email)
            .bind("password", password)
            .mapTo<Int>()
            .singleOrNull()

    override fun getClientByUserId(userId: Int): Client? =
        handle
            .createQuery(
                """
                SELECT u.user_id, u.email, u.password, u.name, c.address
                FROM liftdrop.user u
                JOIN liftdrop.client c ON u.user_id = c.client_id
                WHERE u.user_id = :userId
                """,
            ).bind("userId", userId)
            .mapTo<Client>()
            .singleOrNull()
}
