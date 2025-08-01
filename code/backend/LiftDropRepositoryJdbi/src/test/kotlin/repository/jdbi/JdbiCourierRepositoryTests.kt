package repository.jdbi

import liftdrop.repository.jdbi.JdbiCourierRepository
import liftdrop.repository.jdbi.JdbiUserRepository
import pt.isel.liftdrop.UserRole
import repositoryJdbi.JdbiTestUtils.newTestEmail
import repositoryJdbi.JdbiTestUtils.newTestPassword
import repositoryJdbi.JdbiTestUtils.newTestUserName
import repositoryJdbi.JdbiTestUtils.testWithHandleAndRollback
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JdbiCourierRepositoryTests {
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

                // When: creating a new client associated with the user
                val courierCreation = courierRepository.createCourier(courierId, true)

                if (courierCreation == 0) {
                    throw Exception("Client should be created")
                } else {
                    // Then: the created client should be retrievable from the database
                    val createdClient = courierRepository.getCourierByUserId(courierId)
                    assert(createdClient != null) { "Client should be created" }

                    // Given: the client's email and password
                    val courierEmail = userEmail

                    // When: logging in with the user's email and password
                    val loggedInCourierId = courierRepository.loginCourier(courierEmail, userPassword)
                    // Then: the logged-in client's ID should match the expected client ID
                    assert(loggedInCourierId?.first == courierId) { "Logged in client ID should match" }
                }
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
            assert(loggedInClientId?.first == courierId) { "Logged in client ID should match" }

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
            assert(loggedInClientId?.first == courierId) { "Logged in client ID should match" }

            if (loggedInClientId == null) {
                throw Exception("User should be created")
            }
            // Given: a request ID to decline
            val requestId = 1 // Replace with an actual request ID from your database

            // When: declining the request
            val isDeclined = courierRepository.declineRequest(loggedInClientId.first, requestId)

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
            assert(loggedInClientId?.first == courierId) { "Logged in client ID should match" }

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
            assert(loggedInClientId?.first == courierId) { "Logged in client ID should match" }

            // Given: a request ID associated with a delivery to be completed
            val requestId = 2

            // When: accepting the request to create a delivery
            val isAccepted = courierRepository.acceptRequest(requestId, courierId)
            // Then: the request should be accepted successfully
            assert(isAccepted) { "Request should be accepted" }

            // Given: a pickup pin for the delivery
            val pickupPin = "789012"

            val isPickedUp = courierRepository.pickupDelivery(requestId, courierId, pickupPin)
            // Then: the delivery should be picked up successfully
            assert(isPickedUp) { "Delivery should be picked up" }

            // Given: a drop-off pin for the delivery
            val dropOffPin = "210987"

            // Given: delivery earnings for the completed delivery
            val deliveryEarnings = 5.0 // Example earnings for the delivery

            // When: completing the delivery
            val isCompleted = courierRepository.completeDelivery(requestId, courierId, dropOffPin, deliveryEarnings)

            // Then: the delivery should be completed successfully
            assert(isCompleted) { "Delivery should be completed" }
        }
    }

    @Test
    fun `courier should successfully change his availability status`() {
        testWithHandleAndRollback { handle ->
            // Given: repositories for client and user operations
            val courierRepository = JdbiCourierRepository(handle)
            val userRepository = JdbiUserRepository(handle)

            // Given: an already created courier
            val courierId = 9

            // Given: the courier's email and password
            val user = userRepository.findUserById(courierId) ?: throw Exception("User should be created")
            val userEmail = user.email
            val userPassword = user.password

            // When: logging in with the user's email and password
            val loggedInClientId = courierRepository.loginCourier(userEmail, userPassword)

            // Then: the logged-in client's ID should match the expected client ID
            assert(loggedInClientId?.first == courierId) { "Logged in client ID should match" }

            // When: toggling availability status
            val isToggled = courierRepository.startListening(courierId)

            // Then: the availability status should be toggled successfully
            assert(isToggled) { "Availability status should be toggled" }
        }
    }

    @Test
    fun `closest couriers should be successfully fetched`() {
        testWithHandleAndRollback { handle ->
            // Given: repository for the couriers
            val courierRepository = JdbiCourierRepository(handle)

            val requestId = 1 // Replace with an actual request ID from your database

            // Given: A pickup location
            val pickupLat = 38.75598
            val pickupLon = -9.11446

            // When: Fetching the closest couriers available to a certain pickup spot
            val result = courierRepository.getClosestCouriersAvailable(pickupLat, pickupLon, requestId)
            println("Closest couriers:")
            result.forEach {
                println("Courier ${it.courierId} - Distance: ${"%.2f".format(it.distanceMeters)} meters")
            }

            // Then: the result should be a list of couriers
            assertTrue(result.size <= 5, "Expected at most 5 couriers, got ${result.size}")

            val sorted = result.sortedBy { it.distanceMeters }
            assertEquals(sorted, result, "Couriers should be sorted by ascending distance")

            assertTrue(result.first().courierId == 8)
            assertTrue(result[1].courierId == 4)

            // Then: the couriers should be within 5km of the pickup location

            assertTrue(result.all { it.distanceMeters < 5000.0 }, "All couriers should be within 5km")
        }
    }
}
