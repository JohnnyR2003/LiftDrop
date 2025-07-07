package pt.isel.liftdrop.register.ui.screens

import pt.isel.liftdrop.services.http.Problem

sealed class RegisterScreenState {
    data object Idle : RegisterScreenState()
    data class Register(val isRegistered: Boolean = false) : RegisterScreenState()
    data class Error(val problem: Problem) : RegisterScreenState()
}