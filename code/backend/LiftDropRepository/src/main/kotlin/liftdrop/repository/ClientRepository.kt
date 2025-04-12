package liftdrop.repository

import pt.isel.liftdrop.Client
import pt.isel.pipeline.pt.isel.liftdrop.Address

interface ClientRepository {
    fun createClient(
        clientId: Int,
        address: Address,
    ): Int

    fun loginClient(
        email: String,
        password: String,
    ): Int?

    fun getClientByUserId(userId: Int): Client?
}
