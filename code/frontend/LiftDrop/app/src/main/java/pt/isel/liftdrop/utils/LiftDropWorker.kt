package pt.isel.liftdrop.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import pt.isel.liftdrop.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import pt.isel.liftdrop.ApplicationJsonType
import pt.isel.liftdrop.DependenciesContainer
import pt.isel.liftdrop.HOST
import kotlin.text.toInt

class LiftDropWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        Log.v(TAG, "LiftDropWorker is executing")
        val service = (applicationContext as DependenciesContainer).aboutService
        val locationRepo = (applicationContext as DependenciesContainer).locationRepo
        val courierService = (applicationContext as DependenciesContainer).homeService
        val userInfoRepo = (applicationContext as DependenciesContainer).userInfoRepo
        val httpClient = OkHttpClient()

        return try {
            val location = locationRepo.getCurrentLocation()  // Retry if location isn't available

            val token = userInfoRepo.userInfo?.bearer
                ?: throw IllegalStateException("Token not found")

            val courierId = courierService.getCourierIdByToken(token)

            val body = ("{\"courierId\": ${courierId.toInt()}, \"newLocation\": {" +
                    "\"latitude\": ${location?.latitude}" +
                    ", \"longitude\": ${location?.longitude}" +
                    "}}").toRequestBody(ApplicationJsonType)

            val request = Request.Builder()
                .url("$HOST/courier/updateLocation") // Adjust this to your actual endpoint
                .post(body)
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