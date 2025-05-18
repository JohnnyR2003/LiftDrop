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
import pt.isel.liftdrop.location.LocationRepository
import pt.isel.liftdrop.login.model.LoginService
import pt.isel.liftdrop.login.model.UserInfoRepository
import pt.isel.liftdrop.services.LocationTrackingService
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

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
            incomingRequest = null
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
                _state.update { it.copy(isUserLoggedIn = false, incomingRequest = null, isListening = false) }
            }
        }
    }

    fun fetchDailyEarnings(token: String) {
        viewModelScope.launch {
            try {
                val amount = homeService.getDailyEarnings(token)
                _state.update { it.copy(dailyEarnings = String.format("%.2f", amount)) }
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
                    _state.update { it.copy(incomingRequest = request, isListening = true) }
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
            _state.update { it.copy(isListening = false, incomingRequest = null) }
        }
    }

    fun acceptRequest(
        requestId: String,
        token: String,
        context: Context,
        pickupLat: Double,
        pickupLon: Double,
        dropOffLat: Double,
        dropOffLon: Double
    ) {
        viewModelScope.launch {
            try {
                if (homeService.acceptRequest(requestId, token)) {
                    _state.update { it.copy(incomingRequest = null) }
                    launchNavigationAppChooser(context, pickupLat, pickupLon, dropOffLat, dropOffLon)
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
                _state.update { it.copy(incomingRequest = null) }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Decline error: ${e.message}")
            }
        }
    }

    private fun parseRequestDetails(message: String): CourierRequestDetails {
        val mapper = jacksonObjectMapper()
        val details = mapper.readValue(message, CourierRequestDetails::class.java)
        return CourierRequestDetails(
            requestId = details.requestId,
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
        pickupLat: Double,
        pickupLng: Double,
        dropOffLat: Double,
        dropOffLng: Double
    ) {
        val uri = ("https://www.google.com/maps/dir/?api=1" +
                "&origin=My+Location" +
                "&destination=$dropOffLat,$dropOffLng" +
                "&waypoints=$pickupLat,$pickupLng" +
                "&travelmode=driving").toUri()

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val chooser = Intent.createChooser(intent, "Choose an app for navigation")
        context.startActivity(chooser)
    }

}
