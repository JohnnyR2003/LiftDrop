package pt.isel.liftdrop.register.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pt.isel.liftdrop.DependenciesContainer
import pt.isel.liftdrop.TAG
import pt.isel.liftdrop.login.ui.LoginActivity
import pt.isel.liftdrop.register.model.RegisterViewModel
import pt.isel.liftdrop.register.ui.screens.RegisterScreen
import pt.isel.liftdrop.register.ui.screens.RegisterScreenState
import pt.isel.liftdrop.utils.viewModelInit

class RegisterActivity : ComponentActivity() {

    private val repo by lazy { application as DependenciesContainer }

    private val viewModel: RegisterViewModel by viewModels {
        viewModelInit { RegisterViewModel(repo.loginService, repo.preferencesRepository) }
    }

    companion object {
        fun navigate(origin: Activity) {
            val intent = Intent(origin, RegisterActivity::class.java)
            origin.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "RegisterActivity.onCreate() on process ${Process.myPid()}")

        lifecycleScope.launch {
            viewModel.screenState.collect {
                if (it is RegisterScreenState.Register && it.isRegistered) {
                    LoginActivity.navigate(this@RegisterActivity)
                    viewModel.resetToIdle()
                }
            }
        }

        setContent {
            val state = viewModel.screenState.collectAsState().value
            RegisterScreen(
                isRegistering = state is RegisterScreenState.Register && !state.isRegistered,
                screenState = state,
                onRegisterRequest = { firstName, lastName, email, password ->
                    viewModel.register(
                        username = "$firstName $lastName",
                        email = email,
                        password = password
                    )
                },
                onBackRequest = { finish() }
            )
        }
    }
}