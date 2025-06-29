package pt.isel.liftdrop.home.ui

import pt.isel.liftdrop.home.model.CourierRequestDetails
import pt.isel.liftdrop.home.model.LocationDTO
import pt.isel.liftdrop.services.http.Problem

sealed class HomeScreenState {
    data class Listening(
        val dailyEarnings: String,
        val incomingRequest: Boolean = false,
        val requestDetails: CourierRequestDetails?,
    ) : HomeScreenState()
    data class RequestAccepted(
        val requestId: String,
        val courierId: String,
        val deliveryEarnings: String,
        val pickUpCoordinates: Pair<Double, Double>,
        val dropOffCoordinates: Pair<Double, Double>,
        val deliveryKind: String,
        val deliveryStatus: String = "REQUEST_ACCEPTED",
        val message: String
    ) : HomeScreenState()
    data class RequestDeclined(
        val requestId: String,
        val courierId: String,
        val deliveryEarnings: String,
        val deliveryStatus: String = "REQUEST_DECLINED",
        val message: String
    ) : HomeScreenState()
    data class HeadingToPickUp(
        val deliveryEarnings: String,
        val dropOffCoordinates: Pair<Double, Double>,
        val requestId: String,
        val courierId: String,
        val isPickUpSpotValid : Boolean = false,
        val deliveryKind: String,
        val deliveryStatus: String = "HEADING_TO_PICKUP",
    ) : HomeScreenState()

    data class CurrentOrderInfo(
        val requestId: String,
        val courierId: String,
        val deliveryEarnings: String,
        val pickUpCoordinates: Pair<Double, Double>,
        val dropOffCoordinates: Pair<Double, Double>,
        val deliveryKind: String,
        val deliveryStatus: String = "CURRENT_REQUEST_INFO",
    ) : HomeScreenState()
    data class PickedUp(
        val deliveryEarnings: String,
        val requestId: String,
        val courierId: String,
        val dropOffCoordinates: Pair<Double, Double>,
        val deliveryStatus: String = "PICKED_UP",
    ) : HomeScreenState()
    data class HeadingToDropOff(
        val deliveryEarnings: String,
        val requestId: String,
        val courierId: String,
        val isDropOffSpotValid : Boolean = false,
        val deliveryStatus: String = "HEADING_TO_DROPOFF",
    ) : HomeScreenState()
    data class Delivered(
        val deliveryEarnings: String,
        val deliveryStatus : String = "DELIVERED",
    ) : HomeScreenState()
    data class Idle(
        val dailyEarnings: String,
    ) : HomeScreenState()
    data class Error(val problem: Problem) : HomeScreenState()
    data class Logout(val isDone: Boolean = false) : HomeScreenState()
    data class CancellingOrder(
        val courierId: String,
        val requestId: String,
        val deliveryStatus: String //represents the previous delivery status before cancellation
    ) : HomeScreenState()
    data class CancellingPickup(
        val courierId: String,
        val requestId: String,
    ) : HomeScreenState()
    data class CancellingDropOff(
        val courierId: String,
        val requestId: String,
        val isOrderReassigned: Boolean = false,
        val isOrderPickedUp: Boolean = false,
        val pickUpLocation: LocationDTO? = null,
        val pickupCode: String? = null,
    ) : HomeScreenState()
    data class Offline(
        val message: String = "You are offline. Please check your internet connection.",
    ) : HomeScreenState()
}