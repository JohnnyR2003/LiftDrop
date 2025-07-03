package pt.isel.liftdrop.register.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.isel.liftdrop.login.model.LoginService
import pt.isel.liftdrop.login.model.PreferencesRepository
import pt.isel.liftdrop.register.ui.RegisterScreenState
import pt.isel.liftdrop.services.http.Result

class RegisterViewModel(
    private val service: LoginService,
    preferences: PreferencesRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow<RegisterScreenState>(RegisterScreenState.Idle)
    val screenState: StateFlow<RegisterScreenState> = _screenState.asStateFlow()

    fun register(
        username: String,
        email: String,
        password: String,
    ) {
        if (_screenState.value !is RegisterScreenState.Idle && _screenState.value !is RegisterScreenState.Error)
            throw IllegalStateException("O ViewModel não está em estado Idle ou Error.")
        _screenState.value = RegisterScreenState.Register()
        viewModelScope.launch {
            val result = service.register(
                email = email,
                password = password,
                username = username
            )
            _screenState.value = when (result) {
                is Result.Error -> RegisterScreenState.Error(result.problem)
                is Result.Success -> RegisterScreenState.Register(isRegistered = true)
            }
        }
    }

    fun resetToIdle() {
        _screenState.value = RegisterScreenState.Idle
    }
}