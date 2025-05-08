package pt.isel.liftdrop.location

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun getCurrentLocation(): Location?

    fun getLocationUpdates(): Flow<Location>
}
