package pt.isel.liftdrop.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import pt.isel.liftdrop.HOST
import pt.isel.liftdrop.R // Use o R do seu app!
import pt.isel.liftdrop.services.http.HttpService

class LocationForegroundService : Service() {

    private lateinit var locationService: RealLocationTrackingService

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        val httpClient = OkHttpClient()
        val jsonEncoder = Gson()
        locationService = RealLocationTrackingService(httpClient, applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val authToken = intent?.getStringExtra("authToken")
        val courierId = intent?.getStringExtra("courierId")

        startForeground(1, buildNotification())

        if (authToken.isNullOrBlank() || courierId.isNullOrBlank()) {
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
        Log.v("LocationForegroundService", "Building notification for foreground service")
        val channelId = "location_tracking_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            if (manager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(
                    channelId,
                    "LiftDrop Location Tracking",
                    NotificationManager.IMPORTANCE_HIGH
                )
                manager.createNotificationChannel(channel)
            }
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("LiftDrop is receiving your location")
            .setContentText("Your location is being tracked to ensure timely deliveries.")
            .setSmallIcon(R.drawable.logold) // Use um ícone do seu app!
            .setOngoing(true)
            .build()
    }
}