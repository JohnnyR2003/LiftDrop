package pt.isel.liftdrop.home.model

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pt.isel.liftdrop.home.ui.HomeScreenState
import pt.isel.liftdrop.location.LocationRepository
import pt.isel.liftdrop.login.model.UserInfoRepository
import pt.isel.liftdrop.services.LocationTrackingService

class HomeViewModel(
    private val homeService: HomeService,
    private val locationTrackingService: LocationTrackingService,
    userRepo: UserInfoRepository,
    private val locationRepository: LocationRepository // <-- Add this
) : ViewModel() {

    private val _earnings = MutableStateFlow(0.0)
    val earnings: StateFlow<Double> = _earnings

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoggedIn = MutableStateFlow<Boolean>(userRepo.userInfo != null)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _homeScreenState = MutableStateFlow(HomeScreenState())
    val homeScreenState: StateFlow<HomeScreenState> = _homeScreenState

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _currentRequest = MutableStateFlow<RequestWebSocketDTO?>(null)
    val currentRequest: StateFlow<RequestWebSocketDTO?> = _currentRequest



    fun login() {
        viewModelScope.launch {
            _isLoggedIn.value = true
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoggedIn.value = false
        }
    }

    fun fetchDailyEarnings(userToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _earnings.value = homeService.getDailyEarnings(userToken)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetError() {
        _error.value = null
    }

    fun startLocationUpdates(userToken: String) {
        viewModelScope.launch {
            val location = locationRepository.getCurrentLocation()
            val courierId = homeService.getCourierIdByToken(userToken)
            _currentLocation.value = location
            updateCourierLocation(courierId, location)
        }
    }

    private fun updateCourierLocation(courierId: Int, location: Location?) {
        viewModelScope.launch {
            try {
                homeService.updateCourierLocation(courierId.toString(), location?.latitude, location?.longitude)
                Log.d("HomeViewModel", "Courier location updated: ${location?.latitude}, ${location?.longitude}")
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update courier location"
            }
        }
    }

    fun startListening(token: String) {
        viewModelScope.launch {
            homeService.startListening(
                token = token,
                onMessage = { message ->
                    val request = parseRequest(message)
                    _homeScreenState.update { it.copy(incomingRequest = request) }
                },
                onFailure = { error ->
                    _homeScreenState.update { it.copy(isListening = false) }
                }
            )
            _isListening.value = true
        }
    }

    fun acceptRequest(requestId: String, token: String) {
        viewModelScope.launch {
            try {
                val accepted = homeService.acceptRequest(requestId, token)
                if (accepted) {
                    _homeScreenState.update { it.copy(incomingRequest = null) }
                } else {
                    _error.value = "Failed to accept request"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to accept request"
            }
        }
    }

    fun declineRequest(requestId: String) {
        viewModelScope.launch {
            try {
                homeService.rejectRequest(requestId)
                _homeScreenState.update { it.copy(incomingRequest = null) }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to reject request"
            }
        }
    }

    private fun parseRequest(message: String): CourierRequest {
        // JSON parsing to be done
        return CourierRequest(
            id = "abc123",
            pickup = "McDonald's Downtown",
            dropoff = "123 Main St",
            price = "4.50"
        )
    }

    suspend fun stopListening() {
        homeService.stopListening()
        _isListening.value = false
    }
}
