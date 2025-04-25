package pt.isel.liftdrop.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import pt.isel.liftdrop.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pt.isel.liftdrop.DependenciesContainer

class LiftDropWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        Log.v(TAG, "LiftDropWorker is executing")
        val service = (applicationContext as DependenciesContainer).aboutService
        return try {
            service.getInfo()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}