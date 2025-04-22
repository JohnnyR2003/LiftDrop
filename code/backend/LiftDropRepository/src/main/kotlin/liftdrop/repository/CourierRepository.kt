package liftdrop.repository

import pt.isel.liftdrop.Courier
import pt.isel.liftdrop.CourierWithLocation
import pt.isel.liftdrop.Location

interface CourierRepository {
    fun createCourier(
        userId: Int,
        currentLocation: Location,
        isAvailable: Boolean,
    ): Int

    fun loginCourier(
        email: String,
        password: String,
    ): Int?

    fun acceptRequest(
        requestId: Int,
        courierId: Int,
    ): Boolean

    fun declineRequest(requestId: Int): Boolean

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
        newLocation: Location,
    ): Boolean

    fun toggleAvailability(courierId: Int): Boolean

    fun getClosestCouriersAvailable(
        pickupLat: Double,
        pickupLng: Double,
    ): List<CourierWithLocation>
}
