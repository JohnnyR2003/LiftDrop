package repository.jdbi

import liftdrop.repository.jdbi.JdbiCourierRepository
import liftdrop.repository.jdbi.JdbiUserRepository
import pt.isel.liftdrop.UserRole
import repositoryJdbi.JdbiTestUtils.newTestEmail
import repositoryJdbi.JdbiTestUtils.newTestPassword
import repositoryJdbi.JdbiTestUtils.newTestUserName
import repositoryJdbi.JdbiTestUtils.testWithHandleAndRollback
import kotlin.test.Test

class JdbiCourierRepositoryTests {
    /*
     @Test
     fun `should create courier account and successfully authenticate`() {
         testWithHandleAndRollback { handle ->
             // Given: repositories for client and user operations
             val courierRepository = JdbiCourierRepository(handle)
             val userRepository = JdbiUserRepository(handle)

             // Given: user information for creating a new user
             val userEmail = newTestEmail()
             val userPassword = newTestPassword()
             val userName = newTestUserName()
             val role: UserRole = UserRole.COURIER

             // When: creating a new user
             val userCreation = userRepository.createUser(userEmail, userPassword, userName, role)

             if (userCreation == 0) {
                 throw Exception("User should be created")
             } else {
                 // Then: the user should be retrievable from the database
                 val user = userRepository.findUserByEmail(userEmail) ?: throw Exception("User should be created")

                 // Given: the client id and an address for the client
                 val courierId = user.id
                 val currentLocation = newTestLocation()

                 // When: creating a new client associated with the user
                 val courierCreation = courierRepository.createCourier(courierId, currentLocation, true)

                 if (courierCreation == 0) {
                     throw Exception("Client should be created")
                 } else {
                     // Then: the created client should be retrievable from the database
                     val createdClient = courierRepository.getCourierByUserId(courierId)
                     assert(createdClient != null) { "Client should be created" }

                     // Given: the client's email and password
                     val courierEmail = userEmail
                     val courierPassword = userPassword

                     // When: logging in with the user's email and password
                     val loggedInClientId = courierRepository.loginCourier(courierEmail, courierPassword)
                     // Then: the logged-in client's ID should match the expected client ID
                     assert(loggedInClientId == courierId) { "Logged in client ID should match" }
                 }
             }
         }
     }
     */
    @Test
    fun `should successfully authenticate courier account`() {
        testWithHandleAndRollback { handle ->
            // Given: repositories for client and user operations
            val courierRepository = JdbiCourierRepository(handle)
            val userRepository = JdbiUserRepository(handle)

            // Given: user information for creating a new user
            val userEmail = newTestEmail()
            val userPassword = newTestPassword()
            val userName = newTestUserName()
            val role: UserRole = UserRole.COURIER

            // When: creating a new user
            val userCreation = userRepository.createUser(userEmail, userPassword, userName, role)

            if (userCreation == 0) {
                throw Exception("User should be created")
            } else {
                // Then: the user should be retrievable from the database
                val user = userRepository.findUserByEmail(userEmail) ?: throw Exception("User should be created")

                // Given: the client id and an address for the client
                val courierId = user.id

                // When: logging in with the user's email and password
                val loggedInClientId = courierRepository.loginCourier(userEmail, userPassword)
                // Then: the logged-in client's ID should match the expected client ID
                assert(loggedInClientId == courierId) { "Logged in client ID should match" }
            }
        }
    }

    @Test
    fun `courier should successfully accept request`() {
        testWithHandleAndRollback { handle ->
            // Given: repositories for client and user operations
            val courierRepository = JdbiCourierRepository(handle)
            val userRepository = JdbiUserRepository(handle)

            // Given: an already created courier
            val courierId = 7 // Replace with an actual courier ID from your database

            // Given: the courier's email and password
            val user = userRepository.findUserById(courierId) ?: throw Exception("User should be created")
            val userEmail = user.email
            val userPassword = user.password

            // When: logging in with the user's email and password
            val loggedInClientId = courierRepository.loginCourier(userEmail, userPassword)

            // Then: the logged-in client's ID should match the expected client ID
            assert(loggedInClientId == courierId) { "Logged in client ID should match" }

            // Given: a request ID to accept
            val requestId = 1 // Replace with an actual request ID from your database

            // When: accepting the request
            val isAccepted = courierRepository.acceptRequest(requestId, courierId)

            // Then: the request should be accepted successfully
            assert(isAccepted) { "Request should be accepted" }
        }
    }

    @Test
    fun `courier should successfully decline request`() {
        testWithHandleAndRollback { handle ->
            // Given: repositories for client and user operations
            val courierRepository = JdbiCourierRepository(handle)
            val userRepository = JdbiUserRepository(handle)

            // Given: an already created courier
            val courierId = 5 // Replace with an actual courier ID from your database

            // Given: the courier's email and password
            val user = userRepository.findUserById(courierId) ?: throw Exception("User should be created")
            val userEmail = user.email
            val userPassword = user.password

            // When: logging in with the user's email and password
            val loggedInClientId = courierRepository.loginCourier(userEmail, userPassword)

            // Then: the logged-in client's ID should match the expected client ID
            assert(loggedInClientId == courierId) { "Logged in client ID should match" }

            // Given: a request ID to decline
            val requestId = 1 // Replace with an actual request ID from your database

            // When: declining the request
            val isDeclined = courierRepository.declineRequest(requestId)

            // Then: the request should be declined successfully
            assert(isDeclined) { "Request should be declined" }
        }
    }

    @Test
    fun `courier should successfully cancel delivery`() {
        testWithHandleAndRollback { handle ->
            // Given: repositories for client and user operations
            val courierRepository = JdbiCourierRepository(handle)
            val userRepository = JdbiUserRepository(handle)

            // Given: an already created courier
            val courierId = 6 // Replace with an actual courier ID from your database

            // Given: the courier's email and password
            val user = userRepository.findUserById(courierId) ?: throw Exception("User should be created")
            val userEmail = user.email
            val userPassword = user.password

            // When: logging in with the user's email and password
            val loggedInClientId = courierRepository.loginCourier(userEmail, userPassword)

            // Then: the logged-in client's ID should match the expected client ID
            assert(loggedInClientId == courierId) { "Logged in client ID should match" }

            // Given: a request ID associated with a delivery to be cancelled
            val requestId = 1 // Replace with an actual request ID from your database

            // When: accepting the request to create a delivery
            val isAccepted = courierRepository.acceptRequest(requestId, courierId)
            // Then: the request should be accepted successfully
            assert(isAccepted) { "Request should be accepted" }

            // When: cancelling the delivery
            val isCancelled = courierRepository.cancelDelivery(requestId, courierId)

            // Then: the delivery should be cancelled successfully
            assert(isCancelled) { "Delivery should be cancelled" }
        }
    }

    @Test
    fun `courier should successfully complete a delivery`() {
        testWithHandleAndRollback { handle ->
            // Given: repositories for client and user operations
            val courierRepository = JdbiCourierRepository(handle)
            val userRepository = JdbiUserRepository(handle)

            // Given: an already created courier
            val courierId = 6 // Replace with an actual courier ID from your database

            // Given: the courier's email and password
            val user = userRepository.findUserById(courierId) ?: throw Exception("User should be created")
            val userEmail = user.email
            val userPassword = user.password

            // When: logging in with the user's email and password
            val loggedInClientId = courierRepository.loginCourier(userEmail, userPassword)

            // Then: the logged-in client's ID should match the expected client ID
            assert(loggedInClientId == courierId) { "Logged in client ID should match" }

            // Given: a request ID associated with a delivery to be completed
            val requestId = 1 // Replace with an actual request ID from your database

            // When: accepting the request to create a delivery
            val isAccepted = courierRepository.acceptRequest(requestId, courierId)
            // Then: the request should be accepted successfully
            assert(isAccepted) { "Request should be accepted" }

            // When: completing the delivery
            val isCompleted = courierRepository.completeDelivery(requestId, courierId)

            // Then: the delivery should be completed successfully
            assert(isCompleted) { "Delivery should be completed" }
        }
    }
}
