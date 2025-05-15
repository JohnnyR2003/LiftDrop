package pt.isel.liftdrop

import android.app.Application
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import androidx.work.*
import pt.isel.liftdrop.about.model.AboutService
import pt.isel.liftdrop.about.model.RealAboutService
import pt.isel.liftdrop.home.model.HomeService
import pt.isel.liftdrop.home.model.RealHomeService
import pt.isel.liftdrop.location.LocationRepository
import pt.isel.liftdrop.location.LocationRepositoryImpl
import pt.isel.liftdrop.login.UserInfoSharedPrefs
import java.util.concurrent.TimeUnit
import pt.isel.liftdrop.login.model.LoginService
import pt.isel.liftdrop.login.model.RealLoginService
import pt.isel.liftdrop.login.model.UserInfoRepository
import pt.isel.liftdrop.services.LocationTrackingService
import pt.isel.liftdrop.services.RealLocationTrackingService
import pt.isel.liftdrop.utils.LiftDropWorker


const val TAG = "LiftDropApp"
const val HOST = "https://new-evolving-piranha.ngrok-free.app"
val ApplicationJsonType = "application/json".toMediaType()


/**
 * The contract for the object that holds all the globally relevant dependencies.
 */
interface DependenciesContainer {
    val loginService : LoginService
    val aboutService: AboutService
    val homeService: HomeService
    //val courierService: CourierService
    val userInfoRepo : UserInfoRepository
    val locationTrackingService: LocationTrackingService
    val locationRepo: LocationRepository
}
class LiftDropApplication : DependenciesContainer, Application() {

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            //.cache(Cache(directory = cacheDir, maxSize = 50 * 1024 * 1024))
            .build()
    }

    private val jsonEncoder: Gson by lazy {
        GsonBuilder()
            .create()
    }

    override val loginService: LoginService
        get() = RealLoginService(httpClient, jsonEncoder)

    override val aboutService: AboutService
        get() = RealAboutService(httpClient, jsonEncoder)

    override val homeService: HomeService
        get() = RealHomeService(httpClient, jsonEncoder)

    override val userInfoRepo: UserInfoRepository
        get() = UserInfoSharedPrefs(this)

    override val locationTrackingService: LocationTrackingService
        get() = RealLocationTrackingService(httpClient, jsonEncoder, this)

    override val locationRepo: LocationRepository
        get() = LocationRepositoryImpl(this)

    private val workerConstraints  = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
        .setRequiresCharging(false)
        .build()

    override fun onCreate() {
        super.onCreate()
        Log.v(TAG, "LiftDropApplication.onCreate() on process ${android.os.Process.myPid()}")

        val workRequest =
            PeriodicWorkRequestBuilder<LiftDropWorker>(repeatInterval = 15, TimeUnit.SECONDS)
                .setConstraints(workerConstraints)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "LiftDropWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        Log.v(TAG, "LiftDropWorker was scheduled")
    }
}