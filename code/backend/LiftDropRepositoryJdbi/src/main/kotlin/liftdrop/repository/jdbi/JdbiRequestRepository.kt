package liftdrop.repository.jdbi

import liftdrop.repository.RequestRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.liftdrop.Request
import pt.isel.liftdrop.RequestDetailsDTO

class JdbiRequestRepository(
    private val handle: Handle,
) : RequestRepository {
    override fun createRequest(
        clientId: Int,
        eta: Long,
        pickupCode: String,
        dropoffCode: String,
    ): Int? {
        val clientExists =
            handle
                .createQuery(
                    """
        SELECT EXISTS (
            SELECT 1 FROM liftdrop.client WHERE client_id = :client_id
        )
        """,
                ).bind("client_id", clientId)
                .mapTo<Boolean>()
                .one()

        if (!clientExists) return null

        return handle
            .createUpdate(
                """
        INSERT INTO liftdrop.request (client_id, courier_id, created_at, request_status, eta, pickup_code, dropoff_code)
        
        VALUES (:client_id, NULL, EXTRACT(EPOCH FROM NOW()), :request_status, :eta, :pickup_code, :dropoff_code)
        """,
            ).bind("client_id", clientId)
            .bind("request_status", "PENDING")
            .bind("eta", eta)
            .bind("pickup_code", pickupCode)
            .bind("dropoff_code", dropoffCode)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()
    }

    override fun createRequestDetails(
        requestId: Int,
        description: String,
        quantity: Int,
        pickupLocationId: Int,
        dropoffLocationId: Int,
    ): Int =
        handle
            .createUpdate(
                """
            INSERT INTO liftdrop.request_details (request_id, description, quantity, pickup_location, dropoff_location)
            VALUES (:request_id, :description, :quantity, :pickup_location, :dropoff_location)
            """,
            ).bind("request_id", requestId)
            .bind("description", description)
            .bind("quantity", quantity)
            .bind("pickup_location", pickupLocationId)
            .bind("dropoff_location", dropoffLocationId)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

    override fun updateRequest(
        requestId: Int,
        courierId: Int?,
        requestStatus: String?,
        eta: String?,
    ): Boolean {
        val updateQuery = StringBuilder("UPDATE liftdrop.request SET ")

        if (courierId != null) {
            updateQuery.append("courier_id = :courierId, ")
        }
        if (requestStatus != null) {
            updateQuery.append("request_status = :requestStatus, ")
        }
        if (eta != null) {
            updateQuery.append("eta = :eta, ")
        }

        // Remove the last comma and space
        updateQuery.setLength(updateQuery.length - 2)

        updateQuery.append(" WHERE id = :requestId")

        return handle
            .createUpdate(updateQuery.toString())
            .bind("requestId", requestId)
            .bind("courierId", courierId)
            .bind("requestStatus", requestStatus)
            .bind("eta", eta)
            .execute() > 0
    }

    override fun deleteRequest(requestId: Int): Boolean =
        handle
            .createUpdate(
                """
                DELETE FROM liftdrop.request
                WHERE request_id = :requestId
                """,
            ).bind("requestId", requestId)
            .execute() > 0

    override fun getAllRequestsForClient(clientId: Int): List<Request> =
        handle
            .createQuery(
                """
        SELECT 
            r.request_id,
            r.client_id,
            r.courier_id,
            r.created_at,
            r.request_status,
            r.ETA AS order_eta,
            d.description,
            d.pickup_location,
            d.dropoff_location,
            est.establishment AS restaurant_name
        FROM liftdrop.request r
        JOIN liftdrop.request_details d ON r.request_id = d.request_id
        LEFT JOIN liftdrop.item est ON est.establishment_location = d.pickup_location
        WHERE r.client_id = :client_id
        ORDER BY r.created_at DESC
        """,
            ).bind("client_id", clientId)
            .mapTo<Request>()
            .list()

    override fun getPickupCodeForRequest(requestId: Int): String =
        handle
            .createQuery(
                """
        SELECT pickup_code
        FROM liftdrop.request
        WHERE request_id = :request_id
        """,
            ).bind("request_id", requestId)
            .mapTo<String>()
            .one()

    override fun getPickupCodeForCancelledRequest(requestId: Int): String =
        handle
            .createQuery(
                """
        SELECT pickup_code
        FROM liftdrop.request
        WHERE request_id = :request_id
        """,
            ).bind("request_id", requestId)
            .mapTo<String>()
            .one()

    override fun getRequestById(requestId: Int): Request? =
        handle
            .createQuery(
                """
        SELECT 
            r.request_id,
            r.client_id,
            r.courier_id,
            r.created_at,
            r.request_status,
            r.ETA AS order_eta,
            d.description,
            d.pickup_location,
            d.dropoff_location,
            est.establishment AS restaurant_name
        FROM liftdrop.request r
        JOIN liftdrop.request_details d ON r.request_id = d.request_id
        LEFT JOIN liftdrop.item est ON est.establishment_location = d.pickup_location
        WHERE r.request_id = :request_id
        """,
            ).bind("request_id", requestId)
            .mapTo<Request>()
            .one()

    override fun getRequestForCourierById(requestId: Int): RequestDetailsDTO? =
        handle
            .createQuery(
                """
        SELECT 
            r.request_id,
            d.description,
            l.latitude AS dropoff_latitude,
            l.longitude AS dropoff_longitude,
            a.street AS dropoff_street,
            a.house_number AS dropoff_street_number,
            a.zip_code AS dropoff_postal_code,
            l2.latitude AS pickup_latitude,
            l2.longitude AS pickup_longitude,
            a2.street AS pickup_street,
            a2.house_number AS pickup_street_number,
            a2.zip_code AS pickup_postal_code,
            i.price AS price,
            i.designation AS item,
            d.quantity AS quantity,
            d.dropoff_location
        FROM liftdrop.request r
        JOIN liftdrop.request_details d ON r.request_id = d.request_id
        LEFT JOIN liftdrop.item est ON est.establishment_location = d.pickup_location
        LEFT JOIN liftdrop.location l ON l.location_id = d.dropoff_location
        LEFT JOIN liftdrop.address a ON a.address_id = l.address
        LEFT JOIN liftdrop.location l2 ON l2.location_id = d.pickup_location
        LEFT JOIN liftdrop.address a2 ON a2.address_id = l2.address
        LEFT JOIN liftdrop.item i ON i.designation = d.description
        WHERE d.request_id = :id
        LIMIT 1
        """,
            ).bind("id", requestId)
            .mapTo<RequestDetailsDTO>()
            .findOne()
            .orElse(null)

    override fun getMostRecentRequestIdForClient(clientId: Int): Int? =
        handle
            .createQuery(
                """
        SELECT request_id
        FROM liftdrop.request
        WHERE client_id = :client_id AND request_status = 'DELIVERED'
        ORDER BY created_at DESC
        LIMIT 1
        """,
            ).bind("client_id", clientId)
            .mapTo<Int>()
            .findOne()
            .orElse(null)

    override fun giveRatingToCourier(
        clientId: Int,
        requestId: Int,
        rating: Int,
    ): Boolean {
        // 1. Get the latest delivered request for this client
        val request =
            handle
                .createQuery(
                    """
        SELECT request_id, courier_id
        FROM liftdrop.request
        WHERE client_id = :client_id AND request_status = 'DELIVERED'
        ORDER BY request_id DESC
        LIMIT 1
        """,
                ).bind("client_id", clientId)
                .mapToMap()
                .findOne()
                .orElse(null) ?: return false

        val actualRequestId = request["request_id"] as Int
        val courierId = request["courier_id"] as Int

        // 2. Check if a rating already exists for this request
        val ratingExists =
            handle
                .createQuery(
                    """
        SELECT 1 FROM liftdrop.courier_rating
        WHERE client_id = :client_id AND courier_id = :courier_id AND request_id = :request_id
        LIMIT 1
        """,
                ).bind("client_id", clientId)
                .bind("courier_id", courierId)
                .bind("request_id", actualRequestId)
                .mapTo<Int>()
                .findOne()
                .isPresent

        if (ratingExists) return false

        // 3. Insert the rating for this request
        val inserted =
            handle
                .createUpdate(
                    """
        INSERT INTO liftdrop.courier_rating (courier_id, request_id, client_id, rating)
        VALUES (:courier_id, :request_id, :client_id, :rating)
        """,
                ).bind("courier_id", courierId)
                .bind("request_id", actualRequestId)
                .bind("client_id", clientId)
                .bind("rating", rating)
                .execute() > 0

        if (!inserted) return false

        // 4. Update the courier's average rating
        val avgRating =
            handle
                .createQuery(
                    "SELECT AVG(rating) FROM liftdrop.courier_rating WHERE courier_id = :courier_id",
                ).bind("courier_id", courierId)
                .mapTo<Double>()
                .one()

        handle
            .createUpdate(
                "UPDATE liftdrop.courier SET rating = :avg_rating WHERE courier_id = :courier_id",
            ).bind("avg_rating", avgRating)
            .bind("courier_id", courierId)
            .execute()

        return true
    }

    override fun clear() {
        handle
            .createUpdate(
                """
                    TRUNCATE TABLE liftdrop.request CASCADE;
            """,
            ).execute()
    }
}
