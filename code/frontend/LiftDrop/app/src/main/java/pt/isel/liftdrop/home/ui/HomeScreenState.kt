package pt.isel.liftdrop.home.ui

import pt.isel.liftdrop.home.model.CourierRequestDetails
import pt.isel.liftdrop.services.http.Problem

sealed class HomeScreenState {
    data class Listening(
        val dailyEarnings: String,
        val incomingRequest: Boolean = false,
        val requestDetails: CourierRequestDetails?,
        //val accepted: Boolean = false,
    ) : HomeScreenState()
    data class PickingUp(
        val deliveryEarnings: String,
        val dropoffCoordinates: Pair<Double, Double>? = null,
        val requestId: String,
        val courierId: String,
        //val requestDetails: CourierRequestDetails
        val pickedUp : Boolean = false,
    ) : HomeScreenState()
    data class Delivering(
        val deliveryEarnings: String,
        val requestId: String,
        val courierId: String,
        //val requestDetails: CourierRequestDetails
        val delivered : Boolean = false,
    ) : HomeScreenState()
    data class Delivered(
        val deliveryEarnings: String,
        //val requestDetails: CourierRequestDetails
    ) : HomeScreenState()
    data class Idle(
        val dailyEarnings: String,
    ) : HomeScreenState()
    data class Error(val problem: Problem) : HomeScreenState()
    data class Logout(val isDone: Boolean = false) : HomeScreenState()

}