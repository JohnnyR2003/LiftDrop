package pt.isel.liftdrop.home.ui

import pt.isel.liftdrop.home.model.IncomingRequestDetails
import pt.isel.liftdrop.home.model.LocationDTO
import pt.isel.liftdrop.services.http.Problem

sealed class HomeScreenState {
    data class Listening(
        val dailyEarnings: String,
        val incomingRequest: Boolean = false,
        val requestDetails: IncomingRequestDetails?,
    ) : HomeScreenState()
    data class RequestAccepted(
        val deliveryStatus: String = "REQUEST_ACCEPTED",
        val message: String
    ) : HomeScreenState()
    data class RequestDeclined(
        val deliveryStatus: String = "REQUEST_DECLINED",
        val message: String
    ) : HomeScreenState()
    data class HeadingToPickUp(
        val isPickUpSpotValid : Boolean = false,
        val isOrderInfoVisible : Boolean = false,
        val deliveryStatus: String = "HEADING_TO_PICKUP",
    ) : HomeScreenState()
    data class PickedUp(
        val deliveryStatus: String = "PICKED_UP",
    ) : HomeScreenState()
    data class HeadingToDropOff(
        val isDropOffSpotValid : Boolean = false,
        val isOrderInfoVisible : Boolean = false,
        val deliveryStatus: String = "HEADING_TO_DROPOFF",
    ) : HomeScreenState()
    data class Delivered(
        val dailyEarnings: String,
        val deliveryStatus : String = "DELIVERED",
    ) : HomeScreenState()
    data class Idle(
        val dailyEarnings: String,
    ) : HomeScreenState()
    data class Error(val problem: Problem) : HomeScreenState()
    data class Logout(val isDone: Boolean = false) : HomeScreenState()
    data class CancellingOrder(val deliveryStatus: String) : HomeScreenState()
    data class CancellingPickup(val isDone : Boolean = false, ) : HomeScreenState()
    data class CancellingDropOff(
        val isOrderReassigned: Boolean = false,
        val isOrderPickedUp: Boolean = false,
        val pickUpLocation: LocationDTO? = null,
        val pickupCode: String? = null,
    ) : HomeScreenState()
}