package pt.isel.services

import com.example.ClientRepository
import com.example.UserRepository
import jakarta.inject.Named
import pt.isel.liftdrop.Client


@Named("ClientService")
class ClientService(
    private val clientRepository: ClientRepository,
    private val userRepository: UserRepository,
) {
    fun registerClient(client: Client): Client {
        userRepository.createUser(
            email = client.email,
            password = client.password,
            name = client.name,
        )
        clientRepository.createClient(
            userId = client.id.toInt(),
            address = client.address.toString(),
        )
        return client
    }
    fun loginClient(email: String, password: String): Client? {
        val user = userRepository.findUserByEmail(email) ?: return null
        if (user.password != password) return null
        return clientRepository.getClientByUserId(user.id.toInt())
    }
}