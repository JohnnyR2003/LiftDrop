package pt.isel.liftdrop.home.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.isel.liftdrop.DependenciesContainer
import pt.isel.liftdrop.TAG
import pt.isel.liftdrop.home.model.HomeViewModel
import pt.isel.liftdrop.login.ui.LoginActivity
import pt.isel.liftdrop.services.LocationForegroundService
import pt.isel.liftdrop.utils.viewModelInit
import kotlin.text.compareTo

class HomeActivity : ComponentActivity() {

    private val repo by lazy {
        (application as DependenciesContainer)
    }

    private val viewModel: HomeViewModel by viewModels {
        viewModelInit {
            HomeViewModel(repo.homeService, repo.loginService, repo.locationTrackingService, repo.preferencesRepository)
        }
    }

    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>
    private var notificationPermissionGranted = false

    companion object {
        fun navigate(origin: Activity) {
            val intent = Intent(origin, HomeActivity::class.java)
            origin.startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "HomeActivity.onCreate() on process ${android.os.Process.myPid()}")

        notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            notificationPermissionGranted = isGranted
        }

        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            viewModel.handlePermissions(permissions) { authToken, courierId ->
                startLocationService(this, authToken, courierId)
                Log.i(TAG, "Permission granted. Starting location service with token: $authToken and courierId: $courierId")
            }
        }

        setContent {
            val dailyEarnings = viewModel.dailyEarnings.collectAsState(initial = "0.00").value

            val state = viewModel.stateFlow.collectAsState(initial = HomeScreenState.Idle(dailyEarnings = dailyEarnings )).value

            LaunchedEffect(Unit) {
                val userInfo = repo.preferencesRepository.getUserInfo()
                if (userInfo != null && userInfo.id != 0 && !viewModel.serviceStarted.value) {
                    viewModel.fetchDailyEarnings(userInfo.id.toString(), userInfo.bearer)
                    if (hasLocationPermissions()) {
                        startLocationService(this@HomeActivity, userInfo.bearer, userInfo.id.toString())
                        Log.i(TAG, "Starting location service with token: ${userInfo.bearer} and courierId: ${userInfo.id}")
                        viewModel._serviceStarted.value = true
                    } else {
                        requestAllPermissions()
                    }
                }
            }


            HomeScreen(
                viewModel = viewModel,
                state = state,
                onMenuClick = { /* TODO */ },
                onNotificationClick = { /* TODO */ },
                onStartClick = {
                    lifecycleScope.launch {
                        val user = repo.preferencesRepository.getUserInfo()
                        if (user != null) {
                            if (state is HomeScreenState.Listening) {
                                viewModel.stopListening()
                            } else if(state is HomeScreenState.Idle) {
                                viewModel.startListening()
                            }
                        }
                    }
                },
                onLogoutClick = {
                    viewModel.logout()
                    CoroutineScope(Dispatchers.IO).launch {
                        stopLocationService()
                        LoginActivity.navigate(this@HomeActivity)
                        finish()
                    }
                },
                onCancelDeliveryClick = { viewModel.tryCancelDelivery() },
                dailyEarnings = dailyEarnings,
            )
        }
    }

    fun startLocationService(context: Context, authToken: String, courierId: String) {
        Log.i(TAG, "Starting location service with token: $authToken and courierId: $courierId")
        val intent = Intent(context, LocationForegroundService::class.java).apply {
            putExtra("authToken", authToken)
            putExtra("courierId", courierId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun stopLocationService() {
        val serviceIntent = Intent(this, LocationForegroundService::class.java)
        stopService(serviceIntent)
    }

    private fun hasLocationPermissions(): Boolean {
        val fine = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        val fgLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        } else PackageManager.PERMISSION_GRANTED

        return (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) &&
                fgLocation == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAllPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        }
        locationPermissionLauncher.launch(permissions.toTypedArray())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            notificationPermissionGranted = true
        }
    }
}
