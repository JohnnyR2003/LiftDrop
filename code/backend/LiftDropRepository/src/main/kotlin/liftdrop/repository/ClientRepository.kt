package liftdrop.repository

import pt.isel.liftdrop.Address
import pt.isel.liftdrop.Client

interface ClientRepository {
    fun createClient(
        clientId: Int,
        address: Address,
    ): Int

    fun loginClient(
        email: String,
        password: String,
    ): Pair<Int, String>?

    fun getClientByUserId(userId: Int): Client?

    fun createClientSession(
        userId: Int,
        sessionToken: String,
    ): String?

    fun logoutClient(sessionToken: String): Boolean

    fun getRequestStatus(
        clientId: Int,
        requestId: Int,
    ): Pair<Int, String>?

    fun clear()
}
