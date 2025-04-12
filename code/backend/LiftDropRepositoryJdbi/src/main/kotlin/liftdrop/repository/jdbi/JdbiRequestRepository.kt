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
        requestId: Int,
        courierId: Int?,
        requestStatus: String?,
        eta: String?,
    ): Boolean {
        TODO("Not yet implemented")
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
