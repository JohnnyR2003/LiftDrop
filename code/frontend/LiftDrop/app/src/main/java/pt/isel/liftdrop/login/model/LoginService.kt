package pt.isel.liftdrop.login.model

import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.common.api.Api
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import pt.isel.liftdrop.HOST
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import pt.isel.liftdrop.ApplicationJsonType
import okhttp3.RequestBody.Companion.toRequestBody
import pt.isel.liftdrop.home.model.IntResponse
import pt.isel.liftdrop.location.LocationServices
import pt.isel.liftdrop.login.model.input.RegisterCourierInputModel
import pt.isel.liftdrop.login.model.output.GetCourierIdOutputModel
import pt.isel.liftdrop.login.model.input.LoginInputModel
import pt.isel.liftdrop.login.model.input.LogoutInputModel
import pt.isel.liftdrop.login.model.output.LoginOutputModel
import pt.isel.liftdrop.login.model.output.RegisterOutputModel
import pt.isel.liftdrop.services.http.APIResult
import pt.isel.liftdrop.services.http.HttpService
import java.io.IOException
import java.lang.reflect.Type

interface LoginService {

    suspend fun register(email: String, password: String, username: String): APIResult<RegisterOutputModel>

    suspend fun login(username: String, password: String): APIResult<LoginOutputModel>

    suspend fun logout(token: String, courierId: String): APIResult<Unit>

    suspend fun getCourierIdByToken(token: String): APIResult<GetCourierIdOutputModel>

}

class RealLoginService(
    private val httpService: HttpService
) : LoginService {

    override suspend fun register(email: String, password: String, username: String): APIResult<RegisterOutputModel> {
        val body = RegisterCourierInputModel(
            email = email,
            password = password,
            name = username
        )

        return httpService.post<RegisterOutputModel>(
            endpoint = "/courier/register",
            body = body
        )
    }

    override suspend fun login(email: String, password: String): APIResult<LoginOutputModel> {
        val body = LoginInputModel(
            email = email,
            password = password
        )

        return httpService.post<LoginOutputModel>(
            endpoint = "/courier/login",
            body = body
        )
    }

    override suspend fun logout(token: String, courierId: String): APIResult<Unit> {
        val body = LogoutInputModel(courierId)

        return httpService.delete<Unit>(
            endpoint = "/courier/logout",
            body = body,
            pathParams = mapOf("courierId" to courierId),
            token = token
        )
    }

    override suspend fun getCourierIdByToken(token: String): APIResult<GetCourierIdOutputModel> {
        return httpService.post<GetCourierIdOutputModel>(
            endpoint = "/user/IdByToken",
            token = token
        )
    }

}