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
    ): Int { // first create address based on currentLocation's address then pass its id to create Location and lastly create Courier
        /*val addressId =
            handle
                .createUpdate(
                    """
                INSERT INTO liftdrop.address (country, city, street, house_number, floor, zip_code)
                VALUES (:street, :city, :state, :country, :zipCode)
                """,
                ).bind("country", currentLocation.address.)
                .bind("city", currentLocation.city)

                .executeAndReturnGeneratedKeys()
                .mapTo<Int>()
                .one()
         */
        // check how to generate address fields based on currentLocation's coordinates using google maps API
        val locationId =
            handle
                .createUpdate(
                    """
                INSERT INTO liftdrop.location (latitude, longitude, address)
                VALUES (:latitude, :longitude, :address)
                """,
                ).bind("latitude", currentLocation.latitude)
                .bind("longitude", currentLocation.longitude)
                .bind("address", currentLocation.address)
                .executeAndReturnGeneratedKeys()
                .mapTo<Int>()
                .one()

        return handle
            .createUpdate(
                """
            INSERT INTO liftdrop.courier (courier_id, current_location, is_available)
            VALUES (:courier_id, :current_location, :is_available)
            """,
            ).bind("courier_id", userId)
            .bind("current_location", locationId)
            .bind("is_available", isAvailable)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()
    }

    override fun loginCourier(
        email: String,
        password: String,
    ): Int? =
        handle
            .createQuery(
                """
                SELECT user_id FROM liftdrop.user
                WHERE email = :email AND password = :password AND role = 'COURIER'
                """,
            ).bind("email", email)
            .bind("password", password)
            .mapTo<Int>()
            .singleOrNull()

    override fun acceptRequest(
        requestId: Int,
        courierId: Int,
    ): Boolean {
        val result =
            handle
                .createUpdate(
                    """
                UPDATE liftdrop.request
                SET courier_id = :courierId
                WHERE request_id = :requestId
                """,
                ).bind("courierId", courierId)
                .bind("requestId", requestId)
                .execute()

        return result > 0
    }

    override fun declineRequest(
        requestId: Int,
        courierId: Int,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun cancelDelivery(
        requestId: Int,
        courierId: Int,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun completeRequest(
        requestId: Int,
        courierId: Int,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun getCourierByUserId(userId: Int): Courier? =
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
        courierId: Int,
        newLocation: Location,
    ): Boolean {
        TODO()
    }
}
