package liftdrop.repository.jdbi

import liftdrop.repository.CourierRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.liftdrop.Courier
import pt.isel.liftdrop.Location

class JdbiCourierRepository(
    private val handle: Handle,
) : CourierRepository {
    override fun createCourier(
        userId: Int,
        currentLocation: Location,
        isAvailable: Boolean,
    ): Int =
        handle
            .createUpdate(
                """
                INSERT INTO courier (user_id, current_location, is_available)
                VALUES (:userId, :currentLocation, :isAvailable)
                """,
            ).bind("userId", userId)
            .bind("currentLocation", currentLocation)
            .bind("isAvailable", isAvailable)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

    override fun loginCourier(
        email: String,
        password: String,
    ): Int? =
        handle
            .createQuery(
                """
                SELECT id FROM courier
                WHERE email = :email AND password = :password AND is_courier = true
                """,
            ).bind("email", email)
            .bind("password", password)
            .mapTo<Int>()
            .singleOrNull()

    override fun acceptRequest(
        requestId: Long,
        courierId: Long,
    ): Boolean {
        val result =
            handle
                .createUpdate(
                    """
                UPDATE request
                SET courier_id = :courierId
                WHERE id = :requestId
                """,
                ).bind("courierId", courierId)
                .bind("requestId", requestId)
                .execute()

        return result > 0
    }

    override fun getCourierByUserId(userId: Long): Courier? =
        handle
            .createQuery(
                """
                SELECT * FROM courier
                WHERE user_id = :userId
                """,
            ).bind("userId", userId)
            .mapTo<Courier>()
            .singleOrNull()

    override fun updateCourierLocation(
        courierId: Long,
        newLocation: Location,
    ): Boolean {
        TODO()
    }
}
