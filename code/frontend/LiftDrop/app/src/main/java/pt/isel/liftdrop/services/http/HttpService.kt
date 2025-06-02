package pt.isel.liftdrop.services.http

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import pt.isel.liftdrop.ApplicationJsonType
import java.io.IOException
import java.lang.reflect.Type
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class HttpService(
    val baseUrl: String,
    val client: OkHttpClient,
    val gson: Gson
) {

    suspend inline fun <reified T> get(
        endpoint: String,
        pathParams: Map<String, Any?>? = null,
        queryParams: Map<String, Any?>? = null,
        token: String? = null
    ): T {
        val url = baseUrl + endpoint.params(pathParams, queryParams)
        val request = Request.Builder()
            .url(url)
            .apply { token?.let { addHeader("Authorization", "Bearer $it") } }
            .get()
            .build()

        return request.executeAndParse()
    }

    suspend inline fun <reified T> post(
        endpoint: String,
        body: Any? = null,
        pathParams: Map<String, Any?>? = null,
        queryParams: Map<String, Any?>? = null,
        token: String? = null
    ): T {
        val url = baseUrl + endpoint.params(pathParams, queryParams)
        val jsonBody = gson.toJsonBody(body)
        val request = Request.Builder()
            .url(url)
            .apply { token?.let { addHeader("Authorization", "Bearer $it") } }
            .post(jsonBody)
            .build()

        return request.executeAndParse()
    }

    suspend inline fun <reified T> delete(
        endpoint: String,
        body: Any? = null,
        pathParams: Map<String, Any?>? = null,
        queryParams: Map<String, Any?>? = null,
        token: String? = null
    ): T {
        val url = baseUrl + endpoint.params(pathParams, queryParams)
        val jsonBody = gson.toJsonBody(body)
        val request = Request.Builder()
            .url(url)
            .apply { token?.let { addHeader("Authorization", "Bearer $it") } }
            .delete(jsonBody)
            .build()

        return request.executeAndParse()
    }

    fun Gson.toJsonBody(body: Any?): RequestBody =
        toJson(body).toRequestBody(ApplicationJsonType)

    suspend inline fun <reified T> Request.executeAndParse(): T =
        send(client) { res ->
            when {
                res.isSuccessful -> handleResponse<T>(res, object : TypeToken<T>() {}.type)
                res.code == 502 -> throw InvalidResponseException("Could not connect to server")
                res.body?.isProblem == true -> {
                    val errorJson = res.body?.string()
                    throw ResponseException(gson.fromJson(errorJson, Problem::class.java).toString())
                }
                else -> throw InvalidResponseException(this.body.toString())
            }
        }

    suspend fun <T> Request.send(client: OkHttpClient, handler: (Response) -> T): T =
        suspendCancellableCoroutine { cont ->
            val call = client.newCall(this)
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    cont.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        cont.resume(handler(response))
                    } catch (ex: Throwable) {
                        cont.resumeWithException(ex)
                    }
                }
            })
            cont.invokeOnCancellation { call.cancel() }
        }

    fun <T> handleResponse(response: Response, type: Type): T {
        val contentType = response.body?.contentType()
        val body = response.body?.string()

        if (response.isSuccessful && contentType != null && contentType.subtype == "json" && !body.isNullOrEmpty()) {
            //Log.v("HttpService", "Response successful: ${response.code} - ${response.message}")
            try {
                return gson.fromJson<T>(body, type)
            } catch (e: JsonSyntaxException) {
                Log.e("HttpService", "Invalid JSON syntax: ${e.message}")
                throw UnexpectedResponseException(response)
            }
        } else {
            Log.e("HttpService", "Unexpected response: ${response.code} - ${response.message}")
            throw UnexpectedResponseException(response)
        }
    }

    companion object {
        val ResponseBody.isApplicationJson: Boolean
            get() = contentType()?.subtype == "json"

        val ResponseBody.isProblem: Boolean
            get() = string().contains("type") // crude check, replace with spec-compliant logic

        fun Response.getBodyOrThrow(): ResponseBody =
            body ?: throw InvalidResponseException("Empty response body")


        fun String.params(
            pathParams: Map<String, Any?>? = null,
            queryParams: Map<String, Any?>? = null
        ): String {
            var result = this
            pathParams?.forEach { (key, value) ->
                result = result.replace("{$key}", value.toString())
            }
            queryParams?.let {
                result += "?" + it.map { (k, v) -> "$k=$v" }.joinToString("&")
            }
            return result
        }
    }
}