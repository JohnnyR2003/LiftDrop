package pt.isel.liftdrop.home.model

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import pt.isel.liftdrop.ApplicationJsonType
import pt.isel.liftdrop.HOST
import java.io.IOException

interface HomeService {
    suspend fun getDailyEarnings(token: String): Double

    suspend fun startListening(token: String): Boolean

    suspend fun updateCourierLocation(courierId: String, lat: Double, lon: Double): Boolean

    suspend fun getCourierIdByToken(token: String): Int
}

class RealHomeService(
    private val httpClient: OkHttpClient,
    private val jsonEncoder: Gson
) : HomeService {

    override suspend fun startListening(token: String): Boolean {
    TODO()
    }

    override suspend fun getDailyEarnings(token: String): Double {
        val request = Request.Builder()
            .url("$HOST/earnings/daily/")
            .addHeader("Authorization", "Bearer $token")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("$response")
            val responseBody = response.body?.string()
            return jsonEncoder.fromJson(responseBody, Double::class.java)
        }
    }

    override suspend fun updateCourierLocation(courierId: String, lat: Double, lon: Double): Boolean {
        val body = ("{\"courierId\": ${courierId.toInt()}, \"newLocation\": {" +
                "\"latitude\": $lat" +
                ", \"longitude\": $lon" +
                "}").toRequestBody(ApplicationJsonType)
        val request = Request.Builder()
            .url("$HOST/courier/updateLocation")
            .post(body)
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("$response")
            val responseBody = response.body?.string()
            return jsonEncoder.fromJson(responseBody, Boolean::class.java)
        }
    }

    override suspend fun getCourierIdByToken(token: String): Int {
        val body = ("{\"token\": \"$token\"}").toRequestBody(ApplicationJsonType)
        val request = Request.Builder()
            .url("$HOST/api/user/getIdByToken")
            .post(body)
            .build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("$response")
            val responseBody = response.body?.string()
            return jsonEncoder.fromJson(responseBody, Int::class.java)
        }
    }
}