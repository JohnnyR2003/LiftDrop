package pt.isel.liftdrop.home.ui

import pt.isel.liftdrop.home.model.CourierRequestDetails
import pt.isel.liftdrop.services.http.Problem

sealed class HomeScreenState {
    data class Listening(
        val dailyEarnings: Double,
        val incomingRequest: Boolean = false,
        val requestDetails: CourierRequestDetails?,
        //val accepted: Boolean = false,
    ) : HomeScreenState()
    data class PickingUp(
        val dailyEarnings: Double,
        val dropoffCoordinates: Pair<Double, Double>? = null,
        val requestId: String,
        val courierId: String,
        //val requestDetails: CourierRequestDetails
        val pickedUp : Boolean = false,
    ) : HomeScreenState()
    data class Delivering(
        val dailyEarnings: Double,
        val requestId: String,
        val courierId: String,
        //val requestDetails: CourierRequestDetails
        val delivered : Boolean = false,
    ) : HomeScreenState()
    data class Delivered(
        val dailyEarnings: Double,
        //val requestDetails: CourierRequestDetails
    ) : HomeScreenState()
    data class Idle(
        val dailyEarnings: Double= 0.0,
    ) : HomeScreenState()
    data class Error(val problem: Problem) : HomeScreenState()
    data class Logout(val isDone: Boolean = false) : HomeScreenState()

}