package pt.isel.services

import liftdrop.repository.TransactionManager
import org.springframework.stereotype.Service
import pt.isel.liftdrop.Client
import pt.isel.liftdrop.User
import pt.isel.liftdrop.UserRole
import pt.isel.pipeline.pt.isel.liftdrop.Address

@Service
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
        clientId: Int,
        address: Address,
    ) {
        transactionManager.run {
            val requestRepository = it.requestRepository
            requestRepository.createRequest(
                clientId = clientId,
                eta = 0,
            )
        }
    }
}
