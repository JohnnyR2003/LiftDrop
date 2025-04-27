package pt.isel.liftdrop.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.tasks.await

class LocationServices {

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
    suspend fun getCurrentLocation(context: Context): Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        return fusedLocationClient.lastLocation.await()
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocationCoordinates(context: Context): LatLng? {
        val location = getCurrentLocation(context)
        return location?.let { LatLng(it.latitude, it.longitude) }
    }

    @Composable
    fun GoogleMapWithCurrentLocation() {
        val context = LocalContext.current
        var currentLocation by remember { mutableStateOf<LatLng?>(null) }

        LaunchedEffect(Unit) {
            val location = getCurrentLocation(context)
            currentLocation = location?.let { LatLng(it.latitude, it.longitude) }
        }

        currentLocation?.let { loc ->
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(loc, 15f)
            }

            Box(modifier = Modifier.Companion.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.Companion.fillMaxSize(),
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
            Box(
                modifier = Modifier.Companion.fillMaxSize(),
                contentAlignment = Alignment.Companion.Center
            ) {
                Text("Fetching your location...")
            }
        }
    }

}