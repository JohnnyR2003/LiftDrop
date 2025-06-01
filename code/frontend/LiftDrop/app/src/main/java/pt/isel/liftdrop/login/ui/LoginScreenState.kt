package pt.isel.liftdrop.login.ui

import pt.isel.liftdrop.login.model.UserInfo

sealed class LoginScreenState {
    data object Idle : LoginScreenState()
    data class Login(val userInfo: UserInfo? = null, val isLoggedIn: Boolean = false) :
        LoginScreenState()
    data class Error(val error: Throwable) : LoginScreenState()
}