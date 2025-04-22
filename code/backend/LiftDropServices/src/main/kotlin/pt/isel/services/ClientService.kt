package pt.isel.services

import com.example.utils.Either
import com.example.utils.failure
import com.example.utils.success
import jakarta.inject.Named
import liftdrop.repository.TransactionManager
import pt.isel.liftdrop.Address
import pt.isel.liftdrop.Client
import pt.isel.liftdrop.User
import pt.isel.liftdrop.UserRole

sealed class ClientError {
    data object UserNotFound : ClientError()
    data object InvalidEmailOrPassword : ClientError()
    data object UserAlreadyExists : ClientError()
    data object RequestNotAccepted : ClientError()
}

@Named
class ClientService(
    private val transactionManager: TransactionManager,
) {
    fun registerClient(
        client: User,
        address: Address,
    ): Either<ClientError, Int> =
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
            println("User creation result: $userCreation")
            if (userCreation == 0) {
                failure(ClientError.UserAlreadyExists)
            }

            val id = clientRepository.createClient(
                clientId = userCreation,
                address = address,
            )
            success(id)
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

    fun getClientById(clientId: Int): Client? =
        transactionManager.run {
            val clientRepository = it.clientRepository
            clientRepository.getClientByUserId(clientId)
        }

    fun makeRequest(
        client: Client,
        description: String,
        pickupLocationId: Int,
        dropOffLocationId: Int,
    ): Either<ClientError, Int> {
        return try {
            transactionManager.run { trx ->
                val requestRepository = trx.requestRepository
                val locationRepository = trx.locationRepository

                // Step 1: Create a new request
                val requestId = requestRepository.createRequest(
                    clientId = client.user.id,
                    eta = 0, // Set ETA to null for now, you can change this if needed
                )

                // Step 2: Create request details (pickup and drop-off)
                requestRepository.createRequestDetails(
                    requestId = requestId,
                    description = description,
                    pickupLocationId = pickupLocationId,
                    dropoffLocationId = dropOffLocationId,
                )

                // Return the created request ID on success
                success(requestId)
            }
        } catch (e: Exception) {
            println("Error creating request: ${e.message}")
            // Handle possible exceptions and return the appropriate ClientError
            failure(ClientError.RequestNotAccepted)
        }
    }

}
