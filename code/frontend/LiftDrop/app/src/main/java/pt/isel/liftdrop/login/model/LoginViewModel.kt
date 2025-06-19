package pt.isel.liftdrop.login.model


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pt.isel.liftdrop.login.ui.LoginScreen
import pt.isel.liftdrop.login.ui.LoginScreenState
import pt.isel.liftdrop.services.http.Result

class LoginViewModel(
    private val service: LoginService,
    private val preferences: PreferencesRepository,
) : ViewModel() {

    val stateFlow: Flow<LoginScreenState>
        get() = _stateFlow.asStateFlow()

    internal val _stateFlow: MutableStateFlow<LoginScreenState> =
        MutableStateFlow(LoginScreenState.Idle)

    @Throws(IllegalStateException::class)
    fun login(username: String, password: String) {
        _stateFlow.value = LoginScreenState.Login()
        viewModelScope.launch {
            Log.v("Login", "fetching for login....")
            val result = service.login(username, password)
            Log.v("Login", "fetched done....")
            if (result is Result.Error) {
                Log.v("Login", "fetched failed with ${result.problem}")

                _stateFlow.value = LoginScreenState.Error(result.problem)
            } else if(result is Result.Success) {
                // Log.v("Login", "fetched done and is ${result.getOrNull()}")
                preferences.setUserInfo(
                    UserInfo(
                        id = result.data.id,
                        username = result.data.username,
                        email = result.data.email,
                        bearer = result.data.bearer
                    )
                )
                _stateFlow.value = LoginScreenState.Login(
                    userInfo = UserInfo(
                        id = result.data.id,
                        username = result.data.username,
                        email = result.data.email,
                        bearer = result.data.bearer
                    ),
                    isLoggedIn = true

                )

            }
        }
    }

    fun checkLoginState(token: String){
        viewModelScope.launch {
            _stateFlow.update { it ->
                val userInfo = preferences.getUserInfo()
                val result = service.getCourierIdByToken(token)
                if (result is Result.Error) {
                    Log.v("Login", "Problem fetching courier id with ${result.problem}")
                    if(userInfo != null) {
                        Log.v("Login", "Resposta do backend: ${result.problem}")
                       // Log.v("Login", "Problem  fetching courier id with ${result.problem}, clearing user info")
                        preferences.clearUserInfo(userInfo)
                        LoginScreenState.Idle
                    }
                    it
                }
                else{
                    Log.v("Login", "User is logged in with id $result and token $token")
                     LoginScreenState.Login(
                        userInfo = userInfo,
                        isLoggedIn = true
                    )
                }
            }
        }
    }
}