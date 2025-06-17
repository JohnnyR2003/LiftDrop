package pt.isel.liftdrop.services
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import pt.isel.liftdrop.ApplicationJsonType
import pt.isel.liftdrop.DependenciesContainer
import pt.isel.liftdrop.HOST
import pt.isel.liftdrop.TAG
import pt.isel.liftdrop.home.model.HomeService
import pt.isel.liftdrop.services.http.HttpService
import kotlin.coroutines.resumeWithException

interface LocationTrackingService {
    suspend fun getCurrentLocation(): Location?
    fun startUpdating(authToken: String, courierId: String)
    fun stopUpdating()
    suspend fun sendLocationToBackend(lat: Double, lon: Double, courierId: String, authToken: String)
}

class RealLocationTrackingService(
    private val httpClient: OkHttpClient,
    private val context: Context
) : LocationTrackingService {

    val apiEndpoint = "$HOST/api/courier/updateLocation"
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())

    // Guarda a última localização enviada
    private var lastSentLocation: Location? = null
    private val MIN_DISTANCE_METERS = 50f

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    Log.v(TAG, "Got location: $location")
                    continuation.resume(location, null)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to get location", exception)
                    continuation.resumeWithException(exception)
                }
        }
    }

    @SuppressLint("MissingPermission")
    override fun startUpdating(authToken: String, courierId: String) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 20_000L)
            .setMinUpdateIntervalMillis(20_000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                coroutineScope.launch {
                    val shouldSend = lastSentLocation?.distanceTo(location)?.let { it > MIN_DISTANCE_METERS } != false
                    if (shouldSend) {
                        sendLocationToBackend(location.latitude, location.longitude, courierId, authToken)
                        lastSentLocation = location
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    override fun stopUpdating() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }

    override suspend fun sendLocationToBackend(lat: Double, lon: Double, courierId: String, authToken: String) {
        val body = ("{\"courierId\": ${courierId.toInt()}, \"newLocation\": {" +
                "\"latitude\": $lat" +
                ", \"longitude\": $lon" +
                "}}").toRequestBody(ApplicationJsonType)

        val requestBuilder = Request.Builder()
            .url(apiEndpoint)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer $authToken")

        try {
            val response = httpClient.newCall(requestBuilder.build()).execute()
            if (!response.isSuccessful) {
                println("Failed to send location: ${response.code}")
            }
            response.close()
        } catch (e: Exception) {
            println("Error sending location: ${e.localizedMessage}")
        }
    }
}

