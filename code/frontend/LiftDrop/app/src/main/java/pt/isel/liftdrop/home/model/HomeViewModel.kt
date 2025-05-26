package pt.isel.liftdrop.home.model

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pt.isel.liftdrop.home.ui.HomeScreenState
import pt.isel.liftdrop.login.model.LoginService
import pt.isel.liftdrop.login.model.UserInfoRepository


class HomeViewModel(
    private val homeService: HomeService,
    private val loginService: LoginService,
    private val userRepo: UserInfoRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        HomeScreenState(
            dailyEarnings = "0.00",
            isUserLoggedIn = userRepo.userInfo != null,
            isListening = false,
            incomingRequest = false,
            requestDetails = CourierRequestDetails(
                requestId = "",
                courierId = "",
                pickupLatitude = 0.0,
                pickupLongitude = 0.0,
                dropoffLatitude = 0.0,
                dropoffLongitude = 0.0,
                pickupAddress = "",
                dropoffAddress = "",
                price = "0.0"
            ),
        )
    )
    val homeScreenState: StateFlow<HomeScreenState> = _state.asStateFlow()

    val _serviceStarted = MutableStateFlow<Boolean>(false)
    val serviceStarted: StateFlow<Boolean> = _serviceStarted.asStateFlow()


    fun login() {
        _state.update { it.copy(isUserLoggedIn = true) }
    }

    fun logout(token: String) {
        viewModelScope.launch {
            try {
                loginService.logout(token)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Logout failed: ${e.message}")
            } finally {
                _state.update { it.copy(isUserLoggedIn = false, incomingRequest = false, isListening = false) }
            }
        }
    }

    fun fetchDailyEarnings(courierId: String, token: String) {
        viewModelScope.launch {
            try {
                val amount = homeService.getDailyEarnings(courierId, token)
                _state.update { it.copy(dailyEarnings = amount.toString()) }
                Log.d("HomeViewModel", "Daily earnings fetched: $amount")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Earnings fetch failed: ${e.message}")
            }
        }
    }

    fun startListening(token: String) {
        viewModelScope.launch {
            homeService.startListening(
                token = token,
                onMessage = { message ->
                    val request = parseRequestDetails(message)
                    _state.update { it.copy(incomingRequest = true, isListening = false, requestDetails = request) }
                },
                onFailure = {
                    _state.update { it.copy(isListening = false) }
                }
            )
            _state.update { it.copy(isListening = true) }
        }
    }

    fun stopListening() {
        viewModelScope.launch {
            homeService.stopListening()
            _state.update { it.copy(isListening = false, incomingRequest = false) }
        }
    }

    fun acceptRequest(
        requestId: String,
        token: String,
        context: Context,
        pickupLat: Double,
        pickupLon: Double,
    ) {
        viewModelScope.launch {
            try {
                if (homeService.acceptRequest(requestId, token)) {
                    _state.update { it.copy(incomingRequest = false, isRequestAccepted = true) }
                    launchNavigationAppChooser(context, pickupLat, pickupLon)
                } else {
                    Log.e("HomeViewModel", "Accept request failed")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Accept error: ${e.message}")
            }
        }
    }


    fun declineRequest(requestId: String) {
        viewModelScope.launch {
            try {
                homeService.rejectRequest(requestId)
                _state.update { it.copy(incomingRequest = false, isListening = true) }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Decline error: ${e.message}")
            }
        }
    }

    fun pickupOrder(requestId: String, courierId: String, token: String, context: Context){
        Log.d("HomeViewModel", "pickupOrder() called with requestId: $requestId, courierId: $courierId, token: $token")
        viewModelScope.launch{
            try{
               homeService.pickupOrder(requestId, courierId, token)
                Log.d("HomeViewModel", "Order picked up successfully")
                // Update state to reflect that the order has been picked up
               _state.update { it.copy(isPickedUp = true) }
                launchNavigationAppChooser(
                    context,
                    _state.value.requestDetails!!.dropoffLatitude,
                    _state.value.requestDetails!!.dropoffLongitude
                )
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Pickup error: ${e.message}")
            }
        }
    }

    fun deliverOrder(requestId: String, courierId: String, token: String){
        viewModelScope.launch{
            try{
                homeService.deliverOrder(requestId, courierId, token)
                _state.update { it.copy(isDelivered = true, isRequestAccepted = false, isListening = true) }
                fetchDailyEarnings(courierId, token)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Deliver error: ${e.message}")
            }
        }
    }

    private fun parseRequestDetails(message: String): CourierRequestDetails {
        val mapper = jacksonObjectMapper()
        val details = mapper.readValue(message, CourierRequestDetails::class.java)
        return CourierRequestDetails(
            requestId = details.requestId,
            courierId = details.courierId,
            pickupLatitude = details.pickupLatitude,
            pickupLongitude = details.pickupLongitude,
            dropoffLatitude = details.dropoffLatitude,
            dropoffLongitude = details.dropoffLongitude,
            pickupAddress = details.pickupAddress,
            dropoffAddress = details.dropoffAddress,
            price = details.price
        )
    }
    fun launchNavigationAppChooser(
        context: Context,
        Lat: Double,
        Lng: Double,
    ) {
        // Google Maps URI with pickup as waypoint and drop-off as destination
        val googleMapsUri = ("https://www.google.com/maps/dir/?api=1" +
                "&origin=My+Location" +
                "&destination=$Lat,$Lng" +
                "&travelmode=driving" +
                "&dir_action=navigate").toUri()
        val googleMapsIntent = Intent(Intent.ACTION_VIEW, googleMapsUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        // Waze URI to pickup location only
        val wazeUri = "https://waze.com/ul?ll=$Lat,$Lng&navigate=yes".toUri()
        val wazeIntent = Intent(Intent.ACTION_VIEW, wazeUri).apply {
            setPackage("com.waze")
        }

        // Create chooser intent
        val chooserIntent = Intent.createChooser(googleMapsIntent, "Choose an app for navigation").apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(wazeIntent))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(chooserIntent)
    }

}
