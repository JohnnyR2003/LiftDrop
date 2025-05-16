package pt.isel.liftdrop.login.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import pt.isel.liftdrop.TAG
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import pt.isel.liftdrop.DependenciesContainer
import pt.isel.liftdrop.home.ui.HomeActivity
import pt.isel.liftdrop.login.model.LoginViewModel
import pt.isel.liftdrop.login.model.UserInfo
import pt.isel.liftdrop.utils.SessionManager
import pt.isel.liftdrop.utils.viewModelInit
import kotlin.getValue

class LoginActivity : ComponentActivity() {

    private val repo by lazy {
        (application as DependenciesContainer)
    }
    private val viewModel: LoginViewModel by viewModels {
        viewModelInit {
            LoginViewModel(repo.loginService)
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

        val initialEmail = intent.getStringExtra("email") ?: ""
        val initialPassword = intent.getStringExtra("password") ?: ""

        setContent {
            val loadingState by viewModel.isLoading.collectAsState()
            val token by viewModel.token.collectAsState()
            val error by viewModel.error.collectAsState()

            LoginScreen(
                state = LoginScreenState(token, loadingState, error),
                initialEmail = initialEmail,
                initialPassword = initialPassword,
                onSignInRequest = { username, password ->
                    viewModel.fetchLoginToken(username, password)
                    runBlocking {
                        launch {
                            while (viewModel.isLoading.value);
                            val tok = viewModel.token.value
                            val courierId = viewModel.courierId.value
                            if (tok != null && courierId != "") {
                                repo.userInfoRepo.userInfo = UserInfo(username, tok.token,
                                    courierId.toString()
                                )
                                if(repo.userInfoRepo.userInfo?.bearer != null) {
                                    SessionManager.setUserToken(this@LoginActivity, tok.token)
                                    HomeActivity.navigate(this@LoginActivity)
                                    finish()
                                }
                            }
                        }
                    }
                    viewModel.resetError()
                },
                onBackRequest = {
                    finish()
                },
                onNavigateToRegister = {
                    RegisterActivity.navigate(this@LoginActivity)
                }
            )
        }
    }
}
