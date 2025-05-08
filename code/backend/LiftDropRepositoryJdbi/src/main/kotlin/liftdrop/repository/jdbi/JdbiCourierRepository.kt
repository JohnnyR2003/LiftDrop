package liftdrop.repository.jdbi

import liftdrop.repository.CourierRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.liftdrop.Courier
import pt.isel.liftdrop.CourierWithLocation
import pt.isel.liftdrop.User
import pt.isel.liftdrop.UserRole

class JdbiCourierRepository(
    private val handle: Handle,
) : CourierRepository {
    /**
     * Creates a new courier in the database.
     *
     * @param userId The ID of the user associated with the courier.
     * @param currentLocation The current location of the courier.
     * @param isAvailable Indicates whether the courier is available for requests.
     * @return The ID of the newly created courier.
     */
    @Suppress("ktlint:standard:comment-wrapping")
    override fun createCourier(
        userId: Int,
        isAvailable: Boolean,
    ): Int {
        /*val address= LocationServices.getAddressFromCoordinates(
            latitude = currentLocation.latitude,
            longitude = currentLocation.longitude,
        )*/
        handle
            .createUpdate(
                """
            INSERT INTO liftdrop.courier (courier_id, is_available)
            VALUES (:courier_id, :is_available)
            """,
            ).bind("courier_id", userId)
            .bind("is_available", isAvailable)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

        return userId
    }

    /**
     * Logs in a courier using their email and password.
     *
     * @param email The email of the courier.
     * @param password The password of the courier.
     * @return The ID of the logged-in courier, or null if the login failed.
     */
    override fun loginCourier(
        email: String,
        password: String,
    ): Pair<Int, String>? =
        handle
            .createQuery(
                """
                SELECT c.courier_id, u.password FROM liftdrop.courier c
                JOIN liftdrop."user" u ON c.courier_id = u.user_id
                WHERE u.email = :email
                """,
            ).bind("email", email)
            .map { rs, _ ->
                val id = rs.getInt("courier_id")
                val passwordHash = rs.getString("password")
                Pair(id, passwordHash)
            }.singleOrNull()

    /**
     * Accepts a delivery request for a courier.
     *
     * @param requestId The ID of the delivery request.
     * @param courierId The ID of the courier accepting the request.
     * @return true if the request was accepted successfully, false otherwise.
     */
    override fun acceptRequest(
        requestId: Int,
        courierId: Int,
    ): Boolean {
        // 1. check if request is valid(exists and is pending)

        val request =
            handle
                .createQuery(
                    """
            SELECT request_id FROM liftdrop.request
            WHERE request_id = :request_id
            """,
                ).bind("request_id", requestId)
                .mapTo<Int>()
                .singleOrNull()

        if (request == null) {
            return false // Request not found
        }
        val rowsUpdated =
            handle
                .createUpdate(
                    """
            UPDATE liftdrop.request
            SET courier_id = :courierId,
                request_status = 'ACCEPTED'
            WHERE request_id = :requestId
              AND request_status = 'PENDING'
              AND courier_id IS NULL
            """,
                ).bind("courierId", courierId)
                .bind("requestId", requestId)
                .execute()

        if (rowsUpdated == 1) {
            val updateCourierStatus =
                handle
                    .createUpdate(
                        """
                UPDATE liftdrop.courier
                SET is_available = FALSE
                WHERE courier_id = :courierId
                """,
                    ).bind("courierId", courierId)
                    .execute()

            val createdDelivery =
                handle
                    .createUpdate(
                        """
                INSERT INTO liftdrop.delivery (courier_id, request_id, started_at, completed_at, ETA, delivery_status)
                VALUES (:courierId, :requestId, EXTRACT(EPOCH FROM NOW()), NULL, NULL, 'PENDING')
                """,
                    ).bind("courierId", courierId)
                    .bind("requestId", requestId)
                    .executeAndReturnGeneratedKeys()
                    .mapTo<Int>()
                    .one()

            return (updateCourierStatus > 0 && createdDelivery > 0)
        }
        return false
    }

    /**
     * Declines a delivery request for a courier.
     *
     * @param requestId The ID of the delivery request.
     * @return true if the request was declined successfully, false otherwise.
     */
    override fun declineRequest(
        courierId: Int,
        requestId: Int,
    ): Boolean {
        // 1. Set courier_id = NULL for the request (leave status as PENDING)
        val updateRequest =
            handle
                .createUpdate(
                    """
                    UPDATE liftdrop.request
                    SET courier_id = NULL
                    WHERE request_id = :request_id
                    """.trimIndent(),
                ).bind("request_id", requestId)
                .execute()

        // 2. Insert a row into request_declines
        val updateDeclinedRequests =
            handle
                .createUpdate(
                    """
                    INSERT INTO liftdrop.request_declines (request_id, courier_id)
                    VALUES (:request_id, :courier_id)
                    """.trimIndent(),
                ).bind("request_id", requestId)
                .bind("courier_id", courierId)
                .execute()

        // 3. Return true if the update was successful, false otherwise
        return (updateRequest > 0 && updateDeclinedRequests > 0)
    }

    /**
     * Picks up a delivery for a courier.
     *
     * @param requestId The ID of the delivery request.
     * @param courierId The ID of the courier picking up the delivery.
     * @return true if the delivery was picked up successfully, false otherwise.
     */

    override fun pickupDelivery(
        requestId: Int,
        courierId: Int,
    ): Boolean {
        handle
            .createUpdate(
                """
                UPDATE liftdrop.delivery
                SET delivery_status = 'PICKED_UP'
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
                SET request_status = 'PICKED_UP'
                WHERE request_id = :requestId AND courier_id = :courierId
                """,
                ).bind("requestId", requestId)
                .bind("courierId", courierId)
                .execute()

        return updatedRequest > 0
    }

    /**
     * Cancels a delivery for a courier.
     *
     * @param requestId The ID of the delivery request.
     * @param courierId The ID of the courier cancelling the delivery.
     * @return true if the delivery was cancelled successfully, false otherwise.
     */
    override fun cancelDelivery(
        requestId: Int,
        courierId: Int,
    ): Boolean {
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
                ).bind("requestId", requestId)
                .bind("courierId", courierId)
                .execute()

        return updatedRequest > 0
    }

    /**
     * Completes a delivery for a courier.
     *
     * @param requestId The ID of the delivery request.
     * @param courierId The ID of the courier completing the delivery.
     * @return true if the delivery was completed successfully, false otherwise.
     */
    override fun completeDelivery(
        requestId: Int,
        courierId: Int,
    ): Boolean {
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

    /**
     * Retrieves a courier by their user ID.
     *
     * @param userId The ID of the user associated with the courier.
     * @return The Courier if found, null otherwise.
     */
    override fun getCourierByUserId(userId: Int): Courier? =
        handle
            .createQuery(
                """
                SELECT * FROM liftdrop.courier c join liftdrop."user" u on u.user_id = c.courier_id
                WHERE c.courier_id = :courier_id
                """,
            ).bind("courier_id", userId)
            .map { rs, _ ->
                Courier(
                    user =
                        User(
                            id = rs.getInt("user_id"),
                            email = rs.getString("email"),
                            password = rs.getString("password"),
                            name = rs.getString("name"),
                            role = UserRole.valueOf(rs.getString("role")),
                        ),
                    currentLocation = rs.getInt("current_location"),
                    isAvailable = rs.getBoolean("is_available"),
                )
            }.singleOrNull()

    /**
     * Updates the location of a courier.
     *
     * @param courierId The ID of the courier.
     * @param newLocation The new location of the courier.
     * @return true if the location was updated successfully, false otherwise.
     */
    override fun updateCourierLocation(
        courierId: Int,
        newLocationId: Int,
    ): Boolean {
        val updatedCourier =
            handle
                .createUpdate(
                    """
                UPDATE liftdrop.courier
                SET current_location = :current_location
                WHERE courier_id = :courier_id
                """,
                ).bind("courier_id", courierId)
                .bind("current_location", newLocationId)
                .execute()

        return updatedCourier > 0
    }

    override fun toggleAvailability(courierId: Int): Boolean {
        val updatedCourier =
            handle
                .createUpdate(
                    """
                UPDATE liftdrop.courier
                SET is_available = NOT is_available
                WHERE courier_id = :courier_id
                """,
                ).bind("courier_id", courierId)
                .execute()

        return updatedCourier > 0
    }

    override fun getClosestCouriersAvailable(
        pickupLat: Double,
        pickupLng: Double,
    ): List<CourierWithLocation> =
        handle
            .createQuery(
                """
            SELECT
                c.courier_id,
                l.latitude,
                l.longitude,
                ST_Distance(
                    ST_SetSRID(ST_MakePoint(l.longitude, l.latitude), 4326)::geography,
                    ST_SetSRID(ST_MakePoint(:pickupLon, :pickupLat), 4326)::geography
                ) AS distance_meters
            FROM liftdrop.courier c
            JOIN liftdrop.location l ON c.current_location = l.location_id
            WHERE c.is_available = TRUE
            AND ST_Distance(
                    ST_SetSRID(ST_MakePoint(l.longitude, l.latitude), 4326)::geography,
                    ST_SetSRID(ST_MakePoint(:pickupLon, :pickupLat), 4326)::geography
                ) < 5000  -- This filters couriers with a distance less than 5000 meters
            ORDER BY distance_meters ASC
            LIMIT 5;
            """,
            ).bind("pickupLat", pickupLat)
            .bind("pickupLon", pickupLng)
            .map { rs, _ ->
                CourierWithLocation(
                    courierId = rs.getInt("courier_id"),
                    latitude = rs.getDouble("latitude"),
                    longitude = rs.getDouble("longitude"),
                    distanceMeters = rs.getDouble("distance_meters"),
                )
            }.list()

    override fun createCourierSession(
        userId: Int,
        sessionToken: String,
    ): String? =
        handle
            .createUpdate(
                """
                INSERT INTO liftdrop.sessions (user_id, session_token, created_at, role)
                VALUES (:user_id, :session_token, EXTRACT(EPOCH FROM NOW()), :role)
                RETURNING session_token
                """.trimIndent(),
            ).bind("user_id", userId)
            .bind("session_token", sessionToken)
            .bind("role", UserRole.COURIER.name)
            .executeAndReturnGeneratedKeys()
            .mapTo<String>()
            .singleOrNull()
}
