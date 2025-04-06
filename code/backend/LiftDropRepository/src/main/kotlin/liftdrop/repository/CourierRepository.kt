package liftdrop.repository

import pt.isel.liftdrop.Courier
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
        requestId: Long,
        courierId: Long,
    ): Boolean

    fun getCourierByUserId(userId: Long): Courier?

    fun updateCourierLocation(
        courierId: Long,
        newLocation: Location,
    ): Boolean
}
