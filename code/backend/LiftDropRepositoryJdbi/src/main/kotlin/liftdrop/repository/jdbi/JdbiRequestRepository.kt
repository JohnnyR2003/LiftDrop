package liftdrop.repository.jdbi

import liftdrop.repository.RequestRepository
import org.jdbi.v3.core.Handle

class JdbiRequestRepository(
    private val handle: Handle,
) : RequestRepository {
    override fun createRequest(
        clientId: Int,
        description: String,
        eta: String,
    ): Int {
        TODO("Not yet implemented")
    }

    override fun updateRequest(
        requestId: Long,
        courierId: Long?,
        requestStatus: String?,
        eta: String?,
    ): Boolean {
        val result =
            handle
                .createUpdate(
                    """
                UPDATE request
                SET courier_id = :courierId, request_status = :requestStatus, eta = :eta
                WHERE id = :requestId
                """,
                ).bind("requestId", requestId)
                .bind("courierId", courierId)
                .bind("requestStatus", requestStatus)
                .bind("eta", eta)
                .execute()
        return result > 0
    }

    override fun deleteRequest(requestId: Int): Boolean =
        handle
            .createUpdate(
                """
                DELETE FROM request
                WHERE id = :requestId
                """,
            ).bind("requestId", requestId)
            .execute() > 0
}
