package pt.isel.liftdrop.login.model

import androidx.compose.ui.platform.LocalContext
import androidx.datastore.dataStore
import com.google.android.gms.common.api.Api
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import pt.isel.liftdrop.HOST
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import pt.isel.liftdrop.ApplicationJsonType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.userAgent
import pt.isel.liftdrop.home.model.IntResponse
import pt.isel.liftdrop.location.LocationServices
import pt.isel.liftdrop.login.model.input.RegisterCourierInputModel
import pt.isel.liftdrop.login.model.output.GetCourierIdOutputModel
import pt.isel.liftdrop.login.model.input.LoginInputModel
import pt.isel.liftdrop.login.model.input.LogoutInputModel
import pt.isel.liftdrop.login.model.output.LoginOutputModel
import pt.isel.liftdrop.login.model.output.LogoutOutputModel
import pt.isel.liftdrop.login.model.output.RegisterOutputModel
import pt.isel.liftdrop.login.model.output.UserOutputModel
import pt.isel.liftdrop.services.http.HttpService
import java.io.IOException
import java.lang.reflect.Type

interface LoginService {

    suspend fun register(email: String, password: String, username: String): Int

    suspend fun login(username: String, password: String): UserInfo

    suspend fun logout(token: String, courierId: String): Boolean

    suspend fun getCourierIdByToken(token: String): GetCourierIdOutputModel

}

class RealLoginService(
    private val httpService: HttpService
) : LoginService {

    override suspend fun register(email: String, password: String, username: String): Int {
        val body = RegisterCourierInputModel(
            email = email,
            password = password,
            name = username
        )

        val response = httpService.post<RegisterCourierInputModel, RegisterOutputModel>(
            url = "/courier/register",
            data = body,
            token = "",
        )
        return response.id
    }

    override suspend fun login(email: String, password: String): UserInfo {
        val body = LoginInputModel(
            email = email,
            password = password
        )

        val res = httpService.post<LoginInputModel ,LoginOutputModel>(
            url = "/courier/login",
            data = body,
            token = ""
        )

        return UserInfo(
            courierId = res.id,
            username = res.username,
            email = res.email,
            bearer = res.token
        )
    }

    override suspend fun logout(token: String, courierId: String): Boolean {
        val res = httpService.delete<LogoutOutputModel>(
            url = "/courier/logout",
            token = token
        )
        return res.isLoggedOut
    }

    override suspend fun getCourierIdByToken(token: String): GetCourierIdOutputModel {
        return httpService.get<GetCourierIdOutputModel>(
            url = "/user/IdByToken",
            token = token
        )
    }

}