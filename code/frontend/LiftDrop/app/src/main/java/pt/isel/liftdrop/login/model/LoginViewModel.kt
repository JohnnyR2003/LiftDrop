package pt.isel.liftdrop.login.model


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pt.isel.liftdrop.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginService: LoginService,
) : ViewModel() {

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading = _isLoading.asStateFlow()

    private val _token = MutableStateFlow<Token?>(null)
    val token = _token.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun fetchLoginToken(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _token.value =
                try {
                    loginService.login(username, password)
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                    val errorMessage = e.toString().split(": ").last()
                    _error.value = errorMessage
                    null
                }
            _isLoading.value = false
        }
    }

    fun fetchRegisterToken(email: String, username: String, password: String, location: Pair<Double, Double>?) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _token.value =
                try {
                    loginService.register(email, password, username)
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                    val errorMessage = e.toString().split(": ").last()
                    _error.value = errorMessage
                    null
                }
            _isLoading.value = false
        }
    }

    fun resetError(){
        _error.value = null
    }
}