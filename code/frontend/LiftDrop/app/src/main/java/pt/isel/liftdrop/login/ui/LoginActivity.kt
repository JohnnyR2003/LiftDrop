package pt.isel.liftdrop.login.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import pt.isel.liftdrop.TAG
import kotlinx.coroutines.launch
import pt.isel.liftdrop.DependenciesContainer
import pt.isel.liftdrop.home.ui.HomeActivity
import pt.isel.liftdrop.login.model.LoginViewModel
import pt.isel.liftdrop.login.ui.screens.LoginScreen
import pt.isel.liftdrop.login.ui.screens.LoginScreenState
import pt.isel.liftdrop.register.ui.RegisterActivity
import pt.isel.liftdrop.utils.viewModelInit
import kotlin.getValue

class LoginActivity : ComponentActivity() {

    private val repo by lazy {
        (application as DependenciesContainer)
    }
    private val viewModel: LoginViewModel by viewModels {
        viewModelInit {
            LoginViewModel(
                repo.loginService,
                repo.preferencesRepository
            )
        }
    }

    companion object {
        fun navigate(origin: Activity, email: String = "", password: String = "" ) {
            val intent = Intent(origin, LoginActivity::class.java).apply {
                putExtra("email", email)
                putExtra("password", password)
            }
            origin.startActivity(intent)
            origin.finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "LoginActivity.onCreate() on process ${android.os.Process.myPid()}")

        lifecycleScope.launch {
            viewModel.stateFlow.collect {
                val userInfo = repo.preferencesRepository.getUserInfo()
                if( it is LoginScreenState.Idle && userInfo != null && userInfo.bearer.isNotEmpty()) {
                    viewModel.checkLoginState(userInfo.bearer)
                }
                if (it is LoginScreenState.Login && it.isLoggedIn) {
                    Log.v(TAG, "User logged in successfully through second cycle, navigating to HomeActivity")
                    HomeActivity.navigate(this@LoginActivity)
                }
            }
        }

        setContent {
            val state = viewModel.stateFlow.collectAsState(initial = LoginScreenState.Idle).value

            LoginScreen(
                isLoggingIn = state is LoginScreenState.Login && !state.isLoggedIn,
                screenState = state,
                onSignInRequest = { username, password ->
                    viewModel.login(username, password)
                },
                onNavigateToRegister = {
                    RegisterActivity.navigate(this@LoginActivity)
                },
            )
        }
    }
}
