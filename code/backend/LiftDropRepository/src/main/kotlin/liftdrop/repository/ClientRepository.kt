package liftdrop.repository

import pt.isel.liftdrop.Client

interface ClientRepository {
    fun createClient(
        userId: Int,
        address: String,
    ): Int

    fun loginClient(
        email: String,
        password: String,
    ): Int?

    fun getClientByUserId(userId: Int): Client?
}
