package pt.isel.liftdrop.services.location
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import pt.isel.liftdrop.TAG
import pt.isel.liftdrop.home.model.dto.LocationDTO
import pt.isel.liftdrop.home.model.dto.LocationUpdateInputModel
import pt.isel.liftdrop.services.http.HttpService
import kotlin.coroutines.resumeWithException
import pt.isel.liftdrop.services.http.Result
import pt.isel.liftdrop.shared.model.Uris

interface LocationTrackingService {
    suspend fun getCurrentLocation(): Result<Location>
    fun startUpdating(authToken: String, courierId: String)
    fun stopUpdating()
    suspend fun sendLocationToBackend(
        lat: Double,
        lon: Double,
        courierId: String,
        authToken: String
    ): Result<Boolean>
}

class RealLocationTrackingService(
    private val httpService: HttpService,
    private val context: Context
) : LocationTrackingService {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())

    // Guarda a última localização enviada
    private var lastSentLocation: Location? = null
    private val MIN_DISTANCE_METERS = 50f

    @OptIn(ExperimentalCoroutinesApi::class)
    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Result<Location> {
        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    Log.v(TAG, "Got location: $location")
                    continuation.resume(Result.Success(location), null)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Failed to get location", exception)
                    continuation.resumeWithException(exception)
                }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun startUpdating(authToken: String, courierId: String) {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30_000L)
            .setMinUpdateIntervalMillis(30_000L)
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

    override suspend fun sendLocationToBackend(
        lat: Double,
        lon: Double,
        courierId: String,
        authToken: String
    ): Result<Boolean> {
        val body = LocationUpdateInputModel(
            courierId = courierId.toInt(),
            newLocation = LocationDTO(
                latitude = lat,
                longitude = lon
            )
        )

        return httpService.post<LocationUpdateInputModel, Boolean>(
            url = Uris.Courier.UPDATE_LOCATION,
            data = body,
            token = authToken
        )
    }
}

