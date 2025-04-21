package pt.isel.services

import jakarta.inject.Named
import liftdrop.repository.TransactionManager
import org.springframework.context.annotation.Description
import org.springframework.stereotype.Service
import pt.isel.liftdrop.Client
import pt.isel.liftdrop.User
import pt.isel.liftdrop.UserRole
import pt.isel.pipeline.pt.isel.liftdrop.Address

@Named
class ClientService(
    private val transactionManager: TransactionManager,
) {
    fun registerClient(
        client: User,
        address: Address,
    ): Int =
        transactionManager.run {
            val userRepository = it.usersRepository
            val clientRepository = it.clientRepository

            val userCreation =
                userRepository.createUser(
                    email = client.email,
                    password = client.password,
                    name = client.name,
                    role = UserRole.CLIENT,
                )
            if (userCreation == 0) {
                throw IllegalStateException("User should be created")
            }

            val user = userRepository.findUserByEmail(client.email) ?: throw IllegalStateException("User should be created")
            val clientId = user.id

            clientRepository.createClient(
                clientId = clientId,
                address = address,
            )
        }

    fun getClientById(clientId: Int): Client? =
        transactionManager.run {
            val clientRepository = it.clientRepository
            clientRepository.getClientByUserId(clientId)
        }

    fun loginClient(
        email: String,
        password: String,
    ) {
        transactionManager.run {
            val clientRepository = it.clientRepository
            clientRepository.loginClient(
                email = email,
                password = password,
            )
        }
    }

    fun makeRequest(
        client: Client,
        description: String,
        pickupLocationId: Int,
        dropOffLocationId: Int,
    ) {
        transactionManager.run {
            val requestRepository = it.requestRepository
            val requestId = requestRepository.createRequest(
                clientId = client.user.id,
                eta = 0,
            )

            requestRepository.createRequestDetails(
                requestId = requestId,
                description = description,
                pickupLocationId = pickupLocationId,
                dropoffLocationId = dropOffLocationId,
            )
        }
    }
}
