package liftdrop.repository.jdbi

import liftdrop.repository.RequestRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class JdbiRequestRepository(
    private val handle: Handle,
) : RequestRepository {
    override fun createRequest(
        clientId: Int,
        eta: Long?,
    ): Int {
        val etaF = eta.let { "$it milliseconds" }

        return handle
            .createUpdate(
                """
            INSERT INTO liftdrop.request (client_id, courier_id, created_at, request_status, eta)
            VALUES (:client_id, NULL, NOW(), :request_status, CAST(:eta AS INTERVAL))
            """,
            ).bind("client_id", clientId)
            .bind("request_status", "PENDING")
            .bind("eta", etaF)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()
    }

    override fun createRequestDetails(
        requestId: Int,
        description: String,
        pickupLocationId: Int,
        dropoffLocationId: Int,
    ): Int {
        return handle
            .createUpdate(
                """
            INSERT INTO liftdrop.request_details (request_id, description, pickup_location, dropoff_location)
            VALUES (:request_id, :description, :pickup_location, :dropoff_location)
            """
            )
            .bind("request_id", requestId)
            .bind("description", description)
            .bind("pickup_location", pickupLocationId)
            .bind("dropoff_location", dropoffLocationId)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()
    }

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
}
