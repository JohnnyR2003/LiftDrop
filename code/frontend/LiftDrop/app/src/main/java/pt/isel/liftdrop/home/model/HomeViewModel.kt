package pt.isel.liftdrop.home.model

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pt.isel.liftdrop.location.LocationRepository
import pt.isel.liftdrop.login.model.UserInfoRepository

class HomeViewModel(
    private val homeService: HomeService,
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

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

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
            locationRepository.getLocationUpdates()
                .catch { e -> _error.value = e.message ?: "Location error" }
                .collect { location ->
                    _currentLocation.value = location
                    val courierId = homeService.getCourierIdByToken(userToken)
                    updateCourierLocation(courierId, location)
                }
        }
    }

    private fun updateCourierLocation(courierId: Int, location: Location) {
        viewModelScope.launch {
            try {
                homeService.updateCourierLocation(courierId.toString(), location.latitude, location.longitude)
                Log.d("HomeViewModel", "Courier location updated: ${location.latitude}, ${location.longitude}")
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update courier location"
            }
        }
    }

    fun startListening(userToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                homeService.startListening(userToken)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
