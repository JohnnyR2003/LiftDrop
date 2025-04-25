package pt.isel.liftdrop.home.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.isel.liftdrop.login.model.UserInfoRepository


class HomeViewModel(
    private val homeService: HomeService,
    private val userRepo: UserInfoRepository
) : ViewModel() {

    private val _earnings = MutableStateFlow(0.0)
    val earnings: StateFlow<Double> = _earnings

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoggedIn = MutableStateFlow<Boolean>(userRepo.userInfo != null)
    val isLoggedIn = _isLoggedIn.asStateFlow()

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
}
