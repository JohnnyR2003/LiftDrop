package pt.isel.liftdrop.services

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import pt.isel.liftdrop.HOST
import pt.isel.liftdrop.home.model.HomeService
import pt.isel.liftdrop.home.model.RealHomeService
import pt.isel.liftdrop.services.http.HttpService


class LocationForegroundService : Service() {

    private lateinit var locationService: RealLocationTrackingService

    private val _isRunning = MutableStateFlow<Boolean>(false)
    val isRunning = _isRunning.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        val httpClient = OkHttpClient()
        val jsonEncoder = Gson()
        val httpService = HttpService(HOST, httpClient, jsonEncoder)
        locationService = RealLocationTrackingService(httpClient, applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val authToken = intent?.getStringExtra("authToken")
        val courierId = intent?.getStringExtra("courierId")

        startForeground(1, buildNotification())

        if(authToken == null || courierId == null ||  authToken.isEmpty() || courierId.isEmpty() || courierId.isBlank()) {
            stopSelf()
            return START_NOT_STICKY
        }

        locationService.startUpdating(authToken, courierId)

        _isRunning.value = true

        return START_STICKY
    }

    override fun onDestroy() {
        _isRunning.value = false
        locationService.stopUpdating()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val channelId = "location_tracking_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "LiftDrop Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("LiftDrop is tracking your location")
            .setContentText("You're currently available for deliveries.")
            .setSmallIcon(R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
    }
}