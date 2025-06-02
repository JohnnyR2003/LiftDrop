package pt.isel.liftdrop.about.model

import com.google.gson.Gson
import pt.isel.liftdrop.HOST
import okhttp3.OkHttpClient
import okhttp3.Request
import pt.isel.liftdrop.services.http.HttpService
import java.io.IOException


interface AboutService{

    suspend fun getInfo() : Info
}

class RealAboutService(
    private val httpService: HttpService,
) : AboutService {

    override suspend fun getInfo(): Info {
        return Info("0.1", listOf(Author("teste", "teste", "teste")))
    }
}