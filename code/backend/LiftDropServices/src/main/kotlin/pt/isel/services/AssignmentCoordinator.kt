package pt.isel.services

import kotlinx.coroutines.CompletableDeferred

object AssignmentCoordinator {
    private val pendingResponses = mutableMapOf<Int, CompletableDeferred<Boolean>>()

    fun register(requestId: Int): CompletableDeferred<Boolean> {
        val deferred = CompletableDeferred<Boolean>()
        pendingResponses[requestId] = deferred
        return deferred
    }

    fun complete(
        requestId: Int,
        accepted: Boolean,
    ) {
        pendingResponses.remove(requestId)?.complete(accepted)
    }
}
