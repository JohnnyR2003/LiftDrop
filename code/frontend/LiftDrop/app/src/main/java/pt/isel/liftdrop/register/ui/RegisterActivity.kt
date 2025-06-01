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
import kotlinx.coroutines.runBlocking
import pt.isel.liftdrop.DependenciesContainer
import pt.isel.liftdrop.TAG
import pt.isel.liftdrop.domain.Loaded
import pt.isel.liftdrop.domain.idle
import pt.isel.liftdrop.login.model.LoginViewModel
import pt.isel.liftdrop.login.model.UserInfo
import pt.isel.liftdrop.login.ui.LoginActivity
import pt.isel.liftdrop.login.ui.LoginScreenState
import pt.isel.liftdrop.register.model.RegisterViewModel
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
            viewModel.userId.collect {
                if (it is Loaded) {
                    LoginActivity.navigate(this@RegisterActivity)
                    viewModel.resetToIdle()
                }
            }
        }

        setContent {
            val state = viewModel.userId.collectAsState(initial = idle()).value
            RegisterScreen(
                state = state.toRegisterScreenState(),
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