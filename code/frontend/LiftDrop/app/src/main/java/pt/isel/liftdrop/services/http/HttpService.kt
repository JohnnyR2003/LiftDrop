package pt.isel.liftdrop.services.http

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import pt.isel.liftdrop.ApplicationJsonType
import pt.isel.liftdrop.HOST
import java.io.IOException
import java.lang.reflect.Type
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class HttpService(
    val baseUrl: String,
    val client: OkHttpClient,
    val gson: Gson
) {
    suspend inline fun <reified T> get(url: String, token: String): T =
        suspendCoroutine { continuation ->
            val request = Request
                .Builder()
                .url("$baseUrl$url")
                .addHeader("Authorization", "Bearer $token")
                .build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        if (!response.isSuccessful) {
                            Log.v("Response Body ", responseBody)
                            val failResult =
                                gson.fromJson(responseBody, Problem::class.java)
                            continuation.resumeWithException(IOException(failResult.detail))
                        } else {
                            val result = gson.fromJson<T>(
                                responseBody,
                                object : TypeToken<T>() {}.type
                            )
                            continuation.resumeWith(Result.success(result))
                        }
                    }
                }
            })
        }

    suspend inline fun <reified T, reified R> post(url: String, data: T, token: String): R {
        val json = gson.toJson(data)
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request
            .Builder()
            .url("$baseUrl$url")
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .post(body)
            .build()

        return suspendCoroutine { continuation ->
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.v("Login", e.toString())
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        if (!response.isSuccessful) {
                            val failResult =
                                gson.fromJson(responseBody, Problem::class.java)
                            continuation.resumeWithException(IOException(failResult.detail))
                        } else {
                            val result = gson.fromJson<R>(
                                responseBody,
                                object : TypeToken<R>() {}.type
                            )
                            continuation.resumeWith(Result.success(result))
                        }
                    }
                }
            })
        }
    }

    suspend inline fun <reified T> delete(url: String, token: String): T {
        val request = Request
            .Builder()
            .url("$baseUrl$url")
            .addHeader("Authorization", "Bearer $token")
            .delete()
            .build()

        return suspendCoroutine { continuation ->
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) continuation.resumeWithException(IOException("Unexpected code $response"))
                    val responseBody = response.body?.string()
                    val result = gson.fromJson<T>(responseBody, object : TypeToken<T>() {}.type)
                    continuation.resume(result)
                }
            })
        }
    }
}