package pt.isel.services

import com.example.utils.Either
import com.example.utils.success
import jakarta.inject.Named
import liftdrop.repository.TransactionManager
import pt.isel.liftdrop.Client
import pt.isel.liftdrop.UserRole
import pt.isel.pipeline.pt.isel.liftdrop.Address

sealed class ClientError {
    data object ClientNotFound : ClientError()

    data object UserNotFound : ClientError()

    data object InvalidEmailOrPassword : ClientError()

    data object ClientEmailAlreadyExists : ClientError()
}

@Named
class ClientService(
    private val transactionManager: TransactionManager,
) {
    fun registerClient(
        email: String,
        password: String,
        name: String,
        address: Address,
    ): Either<ClientError, Int> =
        transactionManager.run {
            val userRepository = it.usersRepository
            val clientRepository = it.clientRepository

            val userCreation =
                userRepository.createUser(
                    email = email,
                    password = password,
                    name = name,
                    role = UserRole.CLIENT,
                )

            val user = userRepository.findUserByEmail(email) ?: throw IllegalStateException("User should be created")
            val clientId = user.id

            val clientCreation =
                clientRepository.createClient(
                    clientId = clientId,
                    address = address,
                )

            return@run success(clientCreation)
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
            val requestId =
                requestRepository.createRequest(
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
