package pt.isel.liftdrop.login.model


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import pt.isel.liftdrop.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.isel.liftdrop.login.ui.LoginScreenState
import pt.isel.liftdrop.services.http.APIResult

class LoginViewModel(
    private val service: LoginService,
    private val preferences: PreferencesRepository,
) : ViewModel() {

    val stateFlow: Flow<LoginScreenState>
        get() = _stateFlow.asStateFlow()

    private val _stateFlow: MutableStateFlow<LoginScreenState> =
        MutableStateFlow(LoginScreenState.Idle)

    @Throws(IllegalStateException::class)
    fun login(username: String, password: String) {
        _stateFlow.value = LoginScreenState.Login()
        viewModelScope.launch {
            Log.v("Login", "fetching for login....")
            val result = runCatching { service.login(username, password) }
            Log.v("Login", "fetched done....")
            if (result.isFailure) {
                _stateFlow.value = LoginScreenState.Error(
                    result.exceptionOrNull() ?: Exception("Unknown error")
                )
            } else {
                // Log.v("Login", "fetched done and is ${result.getOrNull()}")
                preferences.setUserInfo(result.getOrThrow())
                _stateFlow.value = LoginScreenState.Login(
                    userInfo = UserInfo(
                        courierId = result.getOrThrow().courierId,
                        username = result.getOrThrow().username,
                        email = result.getOrThrow().email,
                        bearer = result.getOrThrow().bearer
                    ),
                    isLoggedIn = true

                )

            }
        }
    }

    @Throws(IllegalStateException::class)
    fun resetToIdle() {
        check(_stateFlow.value is LoginScreenState.Login || _stateFlow.value is LoginScreenState.Error) {
            "The view model is not in the Login state or in the Error state."
        }
        _stateFlow.value = LoginScreenState.Idle
    }
}