package pt.isel.liftdrop

import android.app.Application
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import pt.isel.liftdrop.home.model.HomeService
import pt.isel.liftdrop.home.model.RealHomeService
import pt.isel.liftdrop.login.preferences.PreferencesDataStore
import pt.isel.liftdrop.login.model.LoginService
import pt.isel.liftdrop.login.preferences.PreferencesRepository
import pt.isel.liftdrop.login.model.RealLoginService
import pt.isel.liftdrop.services.location.LocationTrackingService
import pt.isel.liftdrop.services.location.RealLocationTrackingService
import pt.isel.liftdrop.services.http.HttpService
import java.util.concurrent.TimeUnit


const val TAG = "LiftDropApp"
const val HOST = "https://two025-lift-drop.onrender.com"
//const val HOST = "https://new-evolving-piranha.ngrok-free.app"
val ApplicationJsonType = "application/json".toMediaType()


/**
 * The contract for the object that holds all the globally relevant dependencies.
 */
interface DependenciesContainer {
    val httpService: HttpService
    val loginService : LoginService
    val homeService: HomeService
    val preferencesRepository: PreferencesRepository
    val locationTrackingService: LocationTrackingService
}
class LiftDropApplication : DependenciesContainer, Application() {

    companion object {
        /**
         * The timeout for HTTP requests
         */
        private const val timeout = 10L
        const val LIFTDROP_DATASTORE = "liftdrop_datastore"
    }

    private val dataStore: DataStore<Preferences> by preferencesDataStore(LIFTDROP_DATASTORE)

    override val preferencesRepository: PreferencesRepository
        get() = PreferencesDataStore(dataStore)

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .callTimeout(timeout, TimeUnit.SECONDS)
            .build()
    }

    private val jsonEncoder: Gson by lazy {
        GsonBuilder()
            .create()
    }

    override val httpService: HttpService by lazy {
        HttpService(
            baseUrl = HOST,
            client = httpClient,
            gson = jsonEncoder
        )
    }

    override val loginService: LoginService
        get() = RealLoginService(httpService)

    override val homeService: HomeService
        get() = RealHomeService(httpService)

    override val locationTrackingService: LocationTrackingService
        get() = RealLocationTrackingService(httpService, this)

    override fun onCreate() {
        super.onCreate()
        Log.v(TAG, "LiftDropApplication.onCreate() on process ${android.os.Process.myPid()}")
    }
}