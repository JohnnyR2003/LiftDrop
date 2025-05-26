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
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import pt.isel.liftdrop.DependenciesContainer
import pt.isel.liftdrop.TAG
import pt.isel.liftdrop.login.model.LoginViewModel
import pt.isel.liftdrop.login.model.UserInfo
import pt.isel.liftdrop.utils.viewModelInit

class RegisterActivity : ComponentActivity() {

    private val repo by lazy { application as DependenciesContainer }

    private val viewModel: LoginViewModel by viewModels {
        viewModelInit { LoginViewModel(repo.loginService) }
    }

    companion object {
        fun navigate(origin: Activity) {
            val intent = Intent(origin, RegisterActivity::class.java)
            origin.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "RegisterActivity.onCreate() on process ${android.os.Process.myPid()}")

        setContent {
            val loadingState by viewModel.isLoading.collectAsState()
            val token by viewModel.token.collectAsState()
            val error by viewModel.error.collectAsState()

            RegisterScreen(
                state = LoginScreenState(token, loadingState, error),
                onRegisterRequest = { firstName, lastName, email, password ->
                    viewModel.fetchRegisterToken(email,firstName+lastName, password,Pair(0.0,0.0))
                    runBlocking {
                        launch {
                            while (viewModel.isLoading.value);
                            val tok = viewModel.token.value
                            if (tok != null) {
                                repo.userInfoRepo.userInfo = UserInfo(email, tok.token, "")
                                if(repo.userInfoRepo.userInfo != null) {
                                    LoginActivity.navigate(this@RegisterActivity)
                                    finish()
                                }
                            }
                        }
                    }
                    viewModel.resetError()
                },
                onBackRequest = { finish() }
            )
        }
    }
}
