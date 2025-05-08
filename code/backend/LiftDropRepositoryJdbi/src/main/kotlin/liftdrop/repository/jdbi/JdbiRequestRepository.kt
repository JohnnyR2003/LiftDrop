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
        eta: Long?,
    ): Int =
        handle
            .createUpdate(
                """
            INSERT INTO liftdrop.request (client_id, courier_id, created_at, request_status, eta)
            VALUES (:client_id, NULL, EXTRACT(EPOCH FROM NOW()), :request_status, CAST(:eta AS BIGINT))
            """,
            ).bind("client_id", clientId)
            .bind("request_status", "PENDING")
            .bind("eta", eta)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

    override fun createRequestDetails(
        requestId: Int,
        description: String,
        pickupLocationId: Int,
        dropoffLocationId: Int,
    ): Int =
        handle
            .createUpdate(
                """
            INSERT INTO liftdrop.request_details (request_id, description, pickup_location, dropoff_location)
            VALUES (:request_id, :description, :pickup_location, :dropoff_location)
            """,
            ).bind("request_id", requestId)
            .bind("description", description)
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

    override fun getRequestById(id: Int): Request? =
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
            ).bind("request_id", id)
            .mapTo<Request>()
            .one()

    override fun getRequestForCourierById(id: Int): RequestDetailsDTO =
        handle
            .createQuery(
                """
        SELECT 
            r.request_id,
            d.description,
            l.latitude AS pickup_latitude,
            l.longitude AS pickup_longitude,
            l2.latitude AS dropoff_latitude,
            l2.longitude AS dropoff_longitude,
            d.dropoff_location
        FROM liftdrop.request r
        JOIN liftdrop.request_details d ON r.request_id = d.request_id
        LEFT JOIN liftdrop.item est ON est.establishment_location = d.pickup_location
        LEFT JOIN liftdrop.location l ON l.location_id = d.dropoff_location
        LEFT JOIN liftdrop.location l2 ON l2.location_id = d.pickup_location
        
        WHERE d.request_id = :id
        LIMIT 1
        """,
            ).bind("id", id)
            .mapTo<RequestDetailsDTO>()
            .findOne()
            .orElse(null)
}
