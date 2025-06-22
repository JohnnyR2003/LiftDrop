package liftdrop.repository

import pt.isel.liftdrop.Request
import pt.isel.liftdrop.RequestDetailsDTO

interface RequestRepository {
    fun createRequest(
        clientId: Int,
        eta: Long,
        pickupCode: String,
        dropoffCode: String,
    ): Int?

    fun createRequestDetails(
        requestId: Int,
        description: String,
        pickupLocationId: Int,
        dropoffLocationId: Int,
    ): Int

    fun updateRequest(
        requestId: Int,
        courierId: Int?,
        requestStatus: String?,
        eta: String?,
    ): Boolean

    fun deleteRequest(requestId: Int): Boolean

    fun getAllRequestsForClient(clientId: Int): List<Request>

    fun getRequestById(requestId: Int): Request?

    fun getPickupCodeForCancelledRequest(requestId: Int): String

    fun getRequestForCourierById(requestId: Int): RequestDetailsDTO?

    fun giveRatingToCourier(
        clientId: Int,
        rating: Int,
    ): Boolean

    fun clear()
}
