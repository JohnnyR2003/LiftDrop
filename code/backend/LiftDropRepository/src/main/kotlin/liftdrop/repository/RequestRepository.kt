package liftdrop.repository

interface RequestRepository {
    fun createRequest(
        clientId: Int,
        eta: Long?,
    ): Int

    fun updateRequest(
        requestId: Int,
        courierId: Int?,
        requestStatus: String?,
        eta: String?,
    ): Boolean

    fun deleteRequest(requestId: Int): Boolean
}
