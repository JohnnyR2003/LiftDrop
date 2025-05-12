package pt.isel.liftdrop.home.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.isel.liftdrop.DependenciesContainer
import pt.isel.liftdrop.TAG
import pt.isel.liftdrop.home.model.HomeService
import pt.isel.liftdrop.home.model.HomeViewModel
import pt.isel.liftdrop.login.ui.LoginActivity
import pt.isel.liftdrop.utils.SessionManager.isUserLoggedIn
import pt.isel.liftdrop.utils.viewModelInit

class HomeActivity : ComponentActivity() {


    private val repo by lazy {
        (application as DependenciesContainer)
    }
    private val viewModel: HomeViewModel by viewModels {
        viewModelInit {
            HomeViewModel(repo.homeService, repo.loginService, repo.locationTrackingService, repo.userInfoRepo, repo.locationRepo)
        }
    }

    companion object {
        fun navigate(origin: Activity) {
            val intent = Intent(origin, HomeActivity::class.java)
            origin.startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "HomeActivity.onCreate() on process ${android.os.Process.myPid()}")

        if (!isUserLoggedIn(this)) {
            LoginActivity.navigate(this)
            return
        }

        setContent {
            val loggedState = viewModel.isLoggedIn.collectAsState().value
            val earningsState = viewModel.earnings.collectAsState().value
            val isListening = viewModel.isListening.collectAsState().value
            val homeScreenState = viewModel.homeScreenState.collectAsState().value

            HomeScreen(
                viewModel = viewModel,
                state = HomeScreenState(
                    dailyEarnings = earningsState.toString(),
                    isUserLoggedIn = loggedState,
                    isListening = isListening
                ),
                onMenuClick = { /* TODO */ },
                onNotificationClick = { /* TODO */ },
                onStartClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        if (isListening) {
                            viewModel.stopListening()
                        } else {
                            val userInfo = repo.userInfoRepo.userInfo
                            if (userInfo != null) {
                                viewModel.startListening(token = userInfo.bearer)
                            }
                        }
                    }
                },
                onLogoutClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val userInfo = repo.userInfoRepo.userInfo
                        viewModel.logout(userInfo?.bearer ?: "")
                        LoginActivity.navigate(this@HomeActivity)
                        finish()
                    }
                }
            )
        }
    }
}
