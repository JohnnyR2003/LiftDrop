package pt.isel.liftdrop.login.ui

import pt.isel.liftdrop.login.model.UserInfo
import pt.isel.liftdrop.services.http.Problem

sealed class LoginScreenState {
    data object Idle : LoginScreenState()
    data class Login(val userInfo: UserInfo? = null, val isLoggedIn: Boolean = false) :
        LoginScreenState()
    data class Error(val problem: Problem) : LoginScreenState()
}