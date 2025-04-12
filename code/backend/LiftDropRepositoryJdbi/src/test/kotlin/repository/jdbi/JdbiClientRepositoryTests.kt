package repository.jdbi

import liftdrop.repository.jdbi.JdbiClientRepository
import liftdrop.repository.jdbi.JdbiUserRepository
import pt.isel.liftdrop.UserRole
import repositoryJdbi.JdbiTestUtils.newTestAddress
import repositoryJdbi.JdbiTestUtils.newTestEmail
import repositoryJdbi.JdbiTestUtils.newTestPassword
import repositoryJdbi.JdbiTestUtils.newTestUserName
import repositoryJdbi.JdbiTestUtils.testWithHandleAndRollback
import kotlin.test.Test

class JdbiClientRepositoryTests {
    @Test
    fun `should create client account and successfully authenticate`() {
        testWithHandleAndRollback { handle ->
            // Given: repositories for client and user operations
            val clientRepository = JdbiClientRepository(handle)
            val userRepository = JdbiUserRepository(handle)

            // Given: user information for creating a new user
            val userEmail = newTestEmail()
            val userPassword = newTestPassword()
            val userName = newTestUserName()
            val role: UserRole = UserRole.CLIENT

            // When: creating a new user
            val userCreation = userRepository.createUser(userEmail, userPassword, userName, role)

            if (userCreation == 0) {
                throw Exception("User should be created")
            } else {
                // Then: the user should be retrievable from the database
                val user = userRepository.findUserByEmail(userEmail) ?: throw Exception("User should be created")

                // Given: the client id and an address for the client
                val clientId = user.id
                val address = newTestAddress()

                // When: creating a new client associated with the user
                val clientCreation = clientRepository.createClient(clientId, address)

                if (clientCreation == 0) {
                    throw Exception("Client should be created")
                } else {
                    // Then: the created client should be retrievable from the database
                    val createdClient = clientRepository.getClientByUserId(clientId)
                    assert(createdClient != null) { "Client should be created" }

                    // Given: the client's email and password
                    val clientEmail = userEmail
                    val clientPassword = userPassword

                    // When: logging in with the user's email and password
                    val loggedInClientId = clientRepository.loginClient(clientEmail, clientPassword)
                    // Then: the logged-in client's ID should match the expected client ID
                    assert(loggedInClientId == clientId) { "Logged in client ID should match" }
                }
            }
        }
    }
}
