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
import pt.isel.liftdrop.DependenciesContainer
import pt.isel.liftdrop.TAG
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
            HomeViewModel(repo.homeService, repo.userInfoRepo)
        }
    }

    companion object {
        fun navigate(origin: Activity) {
            val intent = Intent(origin, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            origin.startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "HomeActivity.onCreate() on process ${android.os.Process.myPid()}")

        if (!isUserLoggedIn(this)) {
            LoginActivity.navigate(this)
            finish()
            return
        }

        setContent {
            val loggedState = viewModel.isLoggedIn.collectAsState().value
            val earningsState = viewModel.earnings.collectAsState().value
            HomeScreen(
                state = HomeScreenState(
                    dailyEarnings = earningsState.toString(),
                    isUserLoggedIn = loggedState,
                ),
                onMenuClick = { /* TODO */ },
                onNotificationClick = { /* TODO */ },
                onStartClick = {}
            )
            lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    if (repo.userInfoRepo.userInfo != null) {
                        viewModel.login()
                    }
                }
            })
        }
    }
}
