package pt.isel.services

import jakarta.inject.Named
import liftdrop.repository.TransactionManager
import pt.isel.liftdrop.Client


@Named("ClientService")
class ClientService(
    private val transactionManager: TransactionManager
) {
    fun registerClient(client: Client): Client {
        transactionManager.run {
            val userRepository = it.usersRepository
            val clientRepository = it.clientRepository
            if (userRepository.findUserByEmail(client.email) != null) {
                throw IllegalArgumentException("User with email ${client.email} already exists")
            }
            if (clientRepository.getClientByUserId(client.id.toInt()) != null) {
                throw IllegalArgumentException("Client with id ${client.id} already exists")
            }
            userRepository.createUser(
                email = client.email,
                password = client.password,
                name = client.name,
            )
            clientRepository.createClient(
                userId = client.id.toInt(),
                address = client.address.toString(),
            )
        }
        return client
    }
    fun loginClient(email: String, password: String) {
        transactionManager.run {
            val clientRepository = it.clientRepository
            clientRepository.loginClient(
                email = email,
                password = password,
            )
        }
    }
}