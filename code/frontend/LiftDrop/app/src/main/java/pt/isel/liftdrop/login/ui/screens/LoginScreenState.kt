package pt.isel.liftdrop.login.ui.screens

import pt.isel.liftdrop.domain.UserInfo
import pt.isel.liftdrop.services.http.Problem

sealed class LoginScreenState {
    data object Idle : LoginScreenState()
    data class Login(val userInfo: UserInfo? = null, val isLoggedIn: Boolean = false) :
        LoginScreenState()
    data class Error(val problem: Problem) : LoginScreenState()
}