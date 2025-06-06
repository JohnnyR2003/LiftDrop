package pt.isel.liftdrop.register.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pt.isel.liftdrop.domain.*
import pt.isel.liftdrop.login.model.LoginService
import pt.isel.liftdrop.login.model.PreferencesRepository
import pt.isel.liftdrop.services.http.Result

class RegisterViewModel(
    private val service: LoginService,
    preferences: PreferencesRepository,
) : ViewModel() {

    val userId: Flow<IOState<Int>>
        get() = _createUserIdFlowInfo.asStateFlow()

    private val _createUserIdFlowInfo: MutableStateFlow<IOState<Int>> =
        MutableStateFlow(idle())

    @Throws(IllegalStateException::class)
    fun register(
        username: String,
        email: String,
        password: String,
    ) {
        if (_createUserIdFlowInfo.value !is Idle && _createUserIdFlowInfo.value !is Fail)
            throw IllegalStateException("The view model is not in the idle state or the fail state.")
        _createUserIdFlowInfo.value = loading()
        viewModelScope.launch {
            val result = service.register(
                email = email,
                password = password,
                username = username
            )
            if (result is Result.Error) {
                Log.v("Register", "Error during registration: $result")
                _createUserIdFlowInfo.value =
                    fail(Exception("Error during registration: ${result.problem.title} - ${result.problem.detail}"))
            } else if(result is Result.Success) {
                _createUserIdFlowInfo.value = loaded(kotlin.Result.success(result.data.id))
            }
        }
    }

    /**
     * Resets the view model to the idle state.
     * @throws IllegalStateException If the view model is not in the loaded state or the fail state.
     */
    @Throws(IllegalStateException::class)
    fun resetToIdle() {
        if (_createUserIdFlowInfo.value !is Loaded && _createUserIdFlowInfo.value !is Fail)
            throw IllegalStateException("The view model is not in the loaded state or the fail state.")
        _createUserIdFlowInfo.value = idle()
    }
}