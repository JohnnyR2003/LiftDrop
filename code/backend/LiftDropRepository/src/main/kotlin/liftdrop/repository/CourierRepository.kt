package liftdrop.repository

import pt.isel.liftdrop.Courier
import pt.isel.liftdrop.CourierWithLocation
import pt.isel.liftdrop.LocationDTO

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
    ): Boolean

    fun cancelDelivery(
        requestId: Int,
        courierId: Int,
    ): Boolean

    fun completeDelivery( // if possible should support external pin for completion
        requestId: Int,
        courierId: Int,
    ): Boolean

    fun getCourierByUserId(userId: Int): Courier?

    fun updateCourierLocation(
        courierId: Int,
        newLocation: LocationDTO,
    ): Boolean

    fun toggleAvailability(courierId: Int): Boolean

    fun getClosestCouriersAvailable(
        pickupLat: Double,
        pickupLng: Double,
    ): List<CourierWithLocation>

    fun createCourierSession(
        userId: Int,
        sessionToken: String,
    ): String?
}
