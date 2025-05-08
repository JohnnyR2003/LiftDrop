package pt.isel.liftdrop.services

import androidx.compose.runtime.*
import com.google.accompanist.permissions.*
import android.Manifest
import androidx.compose.material3.Text
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.LatLng
import pt.isel.liftdrop.location.LocationRepository


interface LocationRepository {
    suspend fun getCurrentLocation(): Location?
}
class LocationServices(val locationRepository: LocationRepository) {

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun LocationAwareMap() {
        val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

        LaunchedEffect(Unit) {
            if (!permissionState.status.isGranted) {
                permissionState.launchPermissionRequest()
            }
        }

        if (permissionState.status.isGranted) {
            GoogleMapWithCurrentLocation()
        } else {
            Text("Location permission is required to show the map.")
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        val location = locationRepository.getCurrentLocation()
        return location
    }


    @Composable
    fun GoogleMapWithCurrentLocation() {
        val context = LocalContext.current
        var currentLocation by remember { mutableStateOf<LatLng?>(null) }

        LaunchedEffect(Unit) {
            val location = getCurrentLocation()
            currentLocation = location?.let { LatLng(it.latitude, it.longitude) }
        }

        currentLocation?.let { loc ->
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(loc, 15f)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = true),
                    uiSettings = MapUiSettings(
                        myLocationButtonEnabled = false,
                        compassEnabled = true,
                        mapToolbarEnabled = true,
                        zoomControlsEnabled = false
                    )
                ) {
                    Marker(
                        state = rememberMarkerState(position = loc),
                        title = "You're here"
                    )
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Fetching your location...")
            }
        }
    }

    companion object

}