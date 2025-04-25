package pt.isel.liftdrop.about.model

import com.google.gson.Gson
import pt.isel.liftdrop.HOST
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException


interface AboutService{

    suspend fun getInfo() : Info
}

class RealAboutService(
    private val httpClient: OkHttpClient,
    private val jsonEncoder: Gson
) : AboutService {

    override suspend fun getInfo(): Info {
        val request = Request.Builder()
            .url("$HOST/info/")
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("$response")
            //return response.body.toString()
        }
        return Info("0.1", listOf(Author("teste", "teste", "teste")))
    }
}