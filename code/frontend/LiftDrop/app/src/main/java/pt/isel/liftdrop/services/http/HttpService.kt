package pt.isel.liftdrop.services.http

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class HttpService(
    val baseUrl: String,
    val client: OkHttpClient,
    val gson: Gson
) {
    suspend inline fun <reified T> get(url: String, token: String): Result<T> =
        suspendCancellableCoroutine { continuation ->
            val request = Request.Builder()
                .url("$baseUrl$url")
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Cookie", "auth_token=$token")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resume(Result.Error(
                        Problem(
                            type = "NetworkError",
                            title = "Network Failure",
                            status = 500,
                            detail = e.message)
                    ))
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        if (!response.isSuccessful) {
                            val problem = gson.fromJson(responseBody, Problem::class.java)
                            continuation.resume(Result.Error(problem))
                        } else {
                            val result = gson.fromJson<T>(responseBody, object : TypeToken<T>() {}.type)
                            continuation.resume(Result.Success(result))
                        }
                    } else {
                        continuation.resume(Result.Error(
                            Problem(
                                type = "EmptyResponse",
                                title = "No Content",
                                status = 204,
                                detail = "Response body is null")))
                    }
                }
            })
        }

    suspend inline fun <reified T, reified R> post(url: String, data: T, token: String): Result<R> {
        val json = gson.toJson(data)
        val body = json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("$baseUrl$url")
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Cookie", "auth_token=$token")
            .post(body)
            .build()

        return suspendCancellableCoroutine { continuation ->
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resume(Result.Error(
                        Problem(
                            type = "NetworkError",
                            title = "Network Failure",
                            status = 500,
                            detail = e.message)
                    ))
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        if (!response.isSuccessful) {
                            val problem = gson.fromJson(responseBody, Problem::class.java)
                            continuation.resume(Result.Error(problem))
                        } else {
                            val result = gson.fromJson<R>(responseBody, object : TypeToken<R>() {}.type)
                            continuation.resume(Result.Success(result))
                        }
                    } else {
                        continuation.resume(Result.Error(
                            Problem(
                                type = "EmptyResponse",
                                title = "No Content",
                                status = 204,
                                detail = "Response body is null")
                        ))
                    }
                }
            })
        }
    }

    suspend inline fun <reified T> delete(url: String, token: String): Result<T> {
        val request = Request.Builder()
            .url("$baseUrl$url")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Cookie", "auth_token=$token")
            .delete()
            .build()

        return suspendCancellableCoroutine { continuation ->
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resume(Result.Error(
                        Problem(
                            type = "NetworkError",
                            title = "Network Failure",
                            status = 500,
                            detail = e.message)
                    ))
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        if (!response.isSuccessful) {
                            val problem = gson.fromJson(responseBody, Problem::class.java)
                            continuation.resume(Result.Error(problem))
                        } else {
                            val result = gson.fromJson<T>(responseBody, object : TypeToken<T>() {}.type)
                            continuation.resume(Result.Success(result))
                        }
                    } else {
                        continuation.resume(Result.Error(
                            Problem(
                                type = "EmptyResponse",
                                title = "No Content",
                                status = 204,
                                detail = "Response body is null"
                            )
                        ))
                    }
                }
            })
        }
    }
}