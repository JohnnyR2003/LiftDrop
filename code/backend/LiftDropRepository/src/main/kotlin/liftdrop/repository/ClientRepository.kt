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
    ): Int?

    fun getClientByUserId(userId: Int): Client?
}
