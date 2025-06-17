package pt.isel.services

import kotlinx.coroutines.CompletableDeferred
import pt.isel.pipeline.pt.isel.liftdrop.GlobalLogger.Companion.log
import java.util.concurrent.ConcurrentHashMap

object AssignmentCoordinator {
    private val pendingResponses = ConcurrentHashMap<Int, CompletableDeferred<Boolean>>()

    fun register(requestId: Int): CompletableDeferred<Boolean> {
        val deferred = CompletableDeferred<Boolean>()
        pendingResponses[requestId] = deferred
        return deferred
    }

    fun complete(
        requestId: Int,
        accepted: Boolean,
    ) {
        pendingResponses.remove(requestId)?.complete(accepted) ?: log("Already completed or missing")
    }
}
