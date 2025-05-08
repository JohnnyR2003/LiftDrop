package pt.isel.liftdrop.home.model

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import pt.isel.liftdrop.ApplicationJsonType
import pt.isel.liftdrop.HOST
import java.io.IOException

interface HomeService {
    suspend fun getDailyEarnings(token: String): Double

    suspend fun startListening(token: String, onMessage: (String) -> Unit, onFailure: (Throwable) -> Unit)

    suspend fun stopListening()

    suspend fun acceptRequest(requestId: String, token: String): Boolean

    suspend fun rejectRequest(requestId: String)

    suspend fun updateCourierLocation(courierId: String, lat: Double?, lon: Double?): Boolean

    suspend fun getCourierIdByToken(token: String): Int
}

class RealHomeService(
    private val httpClient: OkHttpClient,
    private val jsonEncoder: Gson
) : HomeService {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    private var isConnected = false

    override suspend fun startListening(
        token: String,
        onMessage: (String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        if (isConnected) return

        val request = Request.Builder()
            .url("$HOST/ws/courier")
            .addHeader("Authorization", "Bearer $token")
            .build()


        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                println("WebSocket connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                onMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                onFailure(t)
                isConnected = false
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(code, reason)
                isConnected = false
                println("WebSocket closing: $code / $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                println("WebSocket closed: $code / $reason")
            }
        })
    }

    override suspend fun stopListening() {
        webSocket?.close(1000, "Client stopped listening")
        webSocket = null
        isConnected = false
    }

    override suspend fun acceptRequest(requestId: String, token: String): Boolean {
        /*val body = ("{\"requestId\": \"$requestId\"}").toRequestBody(ApplicationJsonType)
        val request = Request.Builder()
            .url("$HOST/acceptRequest")
            .post(body)
            .addHeader("Authorization", "Bearer $token")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("$response")
            val responseBody = response.body?.string()
            return jsonEncoder.fromJson(responseBody, Boolean::class.java)
        }*/

        //Send websocket message to accept request
        webSocket?.send("{\"requestId\": \"$requestId\"}")
        TODO()
    }

    override suspend fun rejectRequest(requestId: String) {
        //Send websocket message to reject request
        webSocket?.send("{\"requestId\": \"$requestId\"}")
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

    override suspend fun updateCourierLocation(courierId: String, lat: Double?, lon: Double?): Boolean {
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
        val request = Request.Builder()
            .url("$HOST/user/IdByToken")
            .post(RequestBody.create(null, ByteArray(0))) // empty POST body
            .addHeader("Authorization", "Bearer $token")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("$response")
            val responseBody = response.body?.string()
            return jsonEncoder.fromJson(responseBody, IntResponse::class.java).value
        }
    }
}