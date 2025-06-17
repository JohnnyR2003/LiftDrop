package pt.isel.liftdrop.home.ui

import pt.isel.liftdrop.home.model.CourierRequestDetails
import pt.isel.liftdrop.services.http.Problem

sealed class HomeScreenState {
    data class Listening(
        val dailyEarnings: String,
        val incomingRequest: Boolean = false,
        val requestDetails: CourierRequestDetails?,
    ) : HomeScreenState()
    data class HeadingToPickUp(
        val deliveryEarnings: String,
        val dropOffCoordinates: Pair<Double, Double>,
        val requestId: String,
        val courierId: String,
        val isPickUpSpotValid : Boolean = false,
    ) : HomeScreenState()
    data class PickedUp(
        val deliveryEarnings: String,
        val requestId: String,
        val courierId: String,
        val dropOffCoordinates: Pair<Double, Double>,
    ) : HomeScreenState()
    data class HeadingToDropOff(
        val deliveryEarnings: String,
        val requestId: String,
        val courierId: String,
        val isDropOffSpotValid : Boolean = false,
    ) : HomeScreenState()
    data class Delivered(
        val deliveryEarnings: String,
    ) : HomeScreenState()
    data class Idle(
        val dailyEarnings: String,
    ) : HomeScreenState()
    data class Error(val problem: Problem) : HomeScreenState()
    data class Logout(val isDone: Boolean = false) : HomeScreenState()
    data class Cancelling(
        val courierId: String,
        val requestId: String,
        val isCancelled: Boolean = false,
    ) : HomeScreenState()
}