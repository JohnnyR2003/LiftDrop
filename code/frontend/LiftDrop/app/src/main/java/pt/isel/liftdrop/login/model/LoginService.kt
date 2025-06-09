package pt.isel.liftdrop.login.model

import okhttp3.*
import pt.isel.liftdrop.login.model.input.RegisterCourierInputModel
import pt.isel.liftdrop.login.model.output.GetCourierIdOutputModel
import pt.isel.liftdrop.login.model.input.LoginInputModel
import pt.isel.liftdrop.login.model.output.LoginOutputModel
import pt.isel.liftdrop.login.model.output.LogoutOutputModel
import pt.isel.liftdrop.login.model.output.RegisterOutputModel
import pt.isel.liftdrop.services.http.Result
import pt.isel.liftdrop.services.http.HttpService
import pt.isel.liftdrop.shared.model.Uris

interface LoginService {

    suspend fun register(email: String, password: String, username: String): Result<RegisterOutputModel>

    suspend fun login(username: String, password: String): Result<UserInfo>

    suspend fun logout(token: String, courierId: String): Result<LogoutOutputModel>

    suspend fun getCourierIdByToken(token: String): Result<GetCourierIdOutputModel>

}

class RealLoginService(
    private val httpService: HttpService
) : LoginService {

    override suspend fun register(email: String, password: String, username: String): Result<RegisterOutputModel> {
        val body = RegisterCourierInputModel(
            email = email,
            password = password,
            name = username
        )

        return httpService.post<RegisterCourierInputModel, RegisterOutputModel>(
            url = Uris.Courier.REGISTER,
            data = body,
            token = "",
        )
    }

    override suspend fun login(email: String, password: String): Result<UserInfo> {
        val body = LoginInputModel(
            email = email,
            password = password
        )

        return httpService.post<LoginInputModel , UserInfo>(
            url = Uris.Courier.LOGIN,
            data = body,
            token = ""
        )
    }

    override suspend fun logout(token: String, courierId: String): Result<LogoutOutputModel> {
        return httpService.delete<LogoutOutputModel>(
            url = Uris.Courier.LOGOUT,
            token = token
        )
    }

    override suspend fun getCourierIdByToken(token: String): Result<GetCourierIdOutputModel> {
        return httpService.get<GetCourierIdOutputModel>(
            url = Uris.User.ID_BY_TOKEN,
            token = token
        )
    }
}