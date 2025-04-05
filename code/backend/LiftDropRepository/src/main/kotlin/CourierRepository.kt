package com.example

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

    fun getCourierByUserId(userId: Int): Courier?
}
