package pt.isel.liftdrop.login.model

import androidx.compose.ui.platform.LocalContext
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import pt.isel.liftdrop.HOST
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import pt.isel.liftdrop.ApplicationJsonType
import okhttp3.RequestBody.Companion.toRequestBody
import pt.isel.liftdrop.home.model.IntResponse
import pt.isel.liftdrop.location.LocationServices
import java.io.IOException
import java.lang.reflect.Type

interface LoginService {

    suspend fun register(email: String, password: String, username: String): Token

    suspend fun login(username: String, password: String): Token

    suspend fun logout(token: String, courierId: String): Boolean

    suspend fun getCourierIdByToken(token: String): Int

}

class RealLoginService(
    private val httpClient: OkHttpClient,
    private val jsonEncoder: Gson
) : LoginService {

    override suspend fun register(email: String, password: String, username: String): Token {
        val body = ("{" +
                "\"email\": \"$email\"," +
                "\"password\": \"$password\"," +
                "\"name\": \"$username\"" +
                "}").toRequestBody(ApplicationJsonType)
        val request = Request.Builder()
            .url("$HOST/courier/register")
            .post(body)
            .build()
        httpClient.newCall(request).execute().use { response ->
            val id = handleResponse<String>(response, String::class.java)
            return Token(id)
        }
    }

    override suspend fun login(email: String, password: String): Token {
        val body = ("{" +
                "\"email\": \"$email\"," +
                "\"password\": \"$password\"" +
                "}").toRequestBody(ApplicationJsonType)
        val request = Request.Builder()
            .url("$HOST/courier/login")
            .post(body)
            .build()
        httpClient.newCall(request).execute().use { response ->
            return handleResponse(response, Token::class.java)
        }
    }

    override suspend fun logout(token: String, courierId: String): Boolean {
        val jsonBody = """{"courierId": $courierId}"""
        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$HOST/courier/logout")
            .addHeader("Authorization", "Bearer $token")
            .delete(requestBody)
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                return true
            } else {
                throw ResponseException(response.body?.string().orEmpty())
            }
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

    private fun <T> handleResponse(response: Response, type: Type): T {
        val contentType = response.body?.contentType()
        return if (response.isSuccessful && contentType != null && contentType == ApplicationJsonType) {
            try {
                val body = response.body?.string()
                jsonEncoder.fromJson<T>(body, type)
            } catch (e: JsonSyntaxException) {
                throw UnexpectedResponseException(response)
            }
        } else {
            val body = response.body?.string()
            throw ResponseException(body.orEmpty())
        }
    }

    abstract class ApiException(msg: String) : Exception(msg)

    /**
     * Exception throw when an unexpected response was received from the API.
     */
    class UnexpectedResponseException(
        val response: Response? = null
    ) : ApiException("Unexpected ${response?.code} response from the API.")

    class ResponseException(
        response: String
    ) : ApiException(response)

}