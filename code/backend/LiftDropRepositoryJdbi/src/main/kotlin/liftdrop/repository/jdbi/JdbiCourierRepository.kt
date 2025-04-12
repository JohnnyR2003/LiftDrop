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
    ): Int { // //Needs further integration with google maps API to get address based on coordinates
        val addressId =
            handle
                .createUpdate(
                    """
                INSERT INTO liftdrop.address (country, city, street, house_number, floor, zip_code)
                VALUES (:street, :city, :state, :country, :zipCode)
                """,
                ).bind("country", currentLocation.address)
                .bind("city", currentLocation.address)
                .executeAndReturnGeneratedKeys()
                .mapTo<Int>()
                .one()

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
        val updatedRequest =
            handle
                .createUpdate(
                    """
        UPDATE liftdrop.request
        SET courier_id = :courierId, request_status = 'ACCEPTED'
        WHERE request_id = :requestId
        """,
                ).bind("courierId", courierId)
                .bind("requestId", requestId)
                .execute()

        val createdDelivery =
            handle
                .createUpdate(
                    """
                INSERT INTO liftdrop.delivery (courier_id, request_id, started_at, completed_at, delivery_status)
                VALUES (:courierId, :requestId, NOW(), NULL, 'IN_PROGRESS')
                """,
                ).bind("courierId", courierId)
                .bind("requestId", requestId)
                .executeAndReturnGeneratedKeys()
                .mapTo<Int>()
                .one()

        return createdDelivery > 0
    }

    override fun declineRequest(requestId: Int): Boolean {
        val result =
            handle
                .createUpdate(
                    """
                UPDATE liftdrop.request
                SET courier_id = NULL, request_status = 'DECLINED'
                WHERE request_id = :request_id 
                """,
                ).bind("request_id", requestId)
                .execute()

        return result > 0
    }

    override fun cancelDelivery(
        requestId: Int,
        courierId: Int,
    ): Boolean {
        val updatedDelivery =
            handle
                .createUpdate(
                    """
                UPDATE liftdrop.delivery
                SET delivery_status = 'CANCELLED'
                WHERE request_id = :requestId AND courier_id = :courierId
                """,
                ).bind("courierId", courierId)
                .bind("requestId", requestId)
                .execute()

        val updatedRequest =
            handle
                .createUpdate(
                    """
                UPDATE liftdrop.request
                SET request_status = 'PENDING_CANCELLATION'
                WHERE request_id = :requestId AND courier_id = :courierId
                """,
                ).bind("courierId", courierId)
                .bind("requestId", requestId)
                .execute()

        return updatedRequest > 0
    }

    override fun completeDelivery(
        requestId: Int,
        courierId: Int,
    ): Boolean {
        val updatedDelivery =
            handle
                .createUpdate(
                    """
                UPDATE liftdrop.delivery
                SET delivery_status = 'COMPLETED'
                WHERE request_id = :requestId AND courier_id = :courierId
                """,
                ).bind("courierId", courierId)
                .bind("requestId", requestId)
                .execute()

        val updatedRequest =
            handle
                .createUpdate(
                    """
                UPDATE liftdrop.request
                SET request_status = 'COMPLETED'
                WHERE request_id = :requestId AND courier_id = :courierId
                """,
                ).bind("courierId", courierId)
                .bind("requestId", requestId)
                .execute()

        return updatedRequest > 0
    }

    override fun getCourierByUserId(userId: Int): Courier? =
        handle
            .createQuery(
                """
                SELECT * FROM liftdrop.courier
                WHERE courier_id = :courier_id
                """,
            ).bind("courier_id", userId)
            .mapTo<Courier>()
            .singleOrNull()

    override fun updateCourierLocation(
        courierId: Int,
        newLocation: Location,
    ): Boolean { // Needs further integration with google maps API to get address based on coordinates
        val updatedLocation =
            handle
                .createUpdate(
                    """
                INSERT INTO liftdrop.location (latitude, longitude, address)
                VALUES (:latitude, :longitude, :address)
                ON CONFLICT (latitude, longitude) DO UPDATE
                SET address = :address
                WHERE liftdrop.location.latitude = :latitude AND liftdrop.location.longitude = :longitude
                """,
                ).bind("latitude", newLocation.latitude)
                .bind("longitude", newLocation.longitude)
                .bind("address", newLocation.address)
                .executeAndReturnGeneratedKeys()
                .mapTo<Int>()
                .one()
        // Update the courier's current location

        val updatedCourier =
            handle
                .createUpdate(
                    """
                UPDATE liftdrop.courier
                SET current_location = :current_location
                WHERE courier_id = :courier_id
                """,
                ).bind("courier_id", courierId)
                .bind("current_location", updatedLocation)
                .execute()

        return updatedCourier > 0
    }
}
