package liftdrop.repository

import pt.isel.liftdrop.Courier
import pt.isel.liftdrop.CourierWithLocation

interface CourierRepository {
    fun createCourier(
        userId: Int,
        isAvailable: Boolean,
    ): Int

    fun loginCourier(
        email: String,
        password: String,
    ): Pair<Int, String>?

    fun acceptRequest(
        requestId: Int,
        courierId: Int,
    ): Boolean

    fun declineRequest(
        courierId: Int,
        requestId: Int,
    ): Boolean

    fun pickupDelivery(
        requestId: Int,
        courierId: Int,
        pickupPin: String,
    ): Boolean

    fun cancelDelivery(
        requestId: Int,
        courierId: Int,
    ): Boolean

    fun completeDelivery( // if possible should support external pin for completion
        requestId: Int,
        courierId: Int,
        completionPin: String,
        deliveryEarnings: Double,
    ): Boolean

    fun getCourierByUserId(userId: Int): Courier?

    fun updateCourierLocation(
        courierId: Int,
        newLocationId: Int,
    ): Boolean

    fun startListening(courierId: Int): Boolean

    fun stopListening(courierId: Int): Boolean

    fun getClosestCouriersAvailable(
        pickupLat: Double,
        pickupLng: Double,
        requestId: Int,
        maxDistance: Double = 4000.0, // 4km
    ): List<CourierWithLocation>

    fun fetchDailyEarnings(courierId: Int): Double?

    fun createCourierSession(
        userId: Int,
        sessionToken: String,
    ): String?

    fun logoutCourier(sessionToken: String): Boolean

    fun getCourierIdByCancelledRequest(requestId: Int): Int?

    fun clear()
}
