package pt.isel.liftdrop.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import pt.isel.liftdrop.HOST
import pt.isel.liftdrop.TAG
import pt.isel.liftdrop.DependenciesContainer

class CourierLocationWorker(appContext: Context, params: WorkerParameters)
    : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        Log.v(TAG, "CourierLocationWorker is executing")

        val locationRepo = (applicationContext as DependenciesContainer).locationRepo
        val httpClient = OkHttpClient()

        return try {
            val location = locationRepo.getCurrentLocation()  // Retry if location isn't available

            val requestBody = FormBody.Builder()
                .add("latitude", location?.latitude.toString())
                .add("longitude", location?.longitude.toString())
                .build()

            val request = Request.Builder()
                .url("$HOST/api/courier/location") // Adjust this to your actual endpoint
                .post(requestBody)
                .build()

            withContext(Dispatchers.IO) {
                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        Log.v(TAG, "Location updated successfully")
                        Result.success()
                    } else {
                        Log.e(TAG, "Failed to update location: ${response.code}")
                        Result.retry()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception updating location", e)
            Result.retry()
        }
    }
}
