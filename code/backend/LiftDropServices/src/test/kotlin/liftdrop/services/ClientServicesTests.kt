package liftdrop.services

import com.example.utils.Either
import com.example.utils.Success
import kotlinx.coroutines.runBlocking
import liftdrop.repository.TransactionManager
import liftdrop.repository.jdbi.JdbiTransactionManager
import liftdrop.repository.jdbi.configureWithAppRequirements
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.BeforeEach
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.liftdrop.Address
import pt.isel.liftdrop.Client
import pt.isel.liftdrop.User
import pt.isel.liftdrop.UserRole
import pt.isel.services.CourierWebSocketHandler
import pt.isel.services.assignment.AssignmentServices
import pt.isel.services.client.ClientService
import pt.isel.services.courier.CourierService
import pt.isel.services.google.GeocodingServices
import pt.isel.services.user.UserService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ClientServiceTest {
    companion object {
        private val jdbi =
            Jdbi
                .create(
                    PGSimpleDataSource().apply {
                        setURL(Environment.getDbUrl())
                    },
                ).configureWithAppRequirements()

        private val transactionManager = JdbiTransactionManager(jdbi)

        private val userService = createUserService(transactionManager)

        private val courierService = createCourierService()

        private val courierWebSocketHandler = createCourierWebSocketHandler()

        private val geocodingServices = createGeocodingService()

        private val assignmentServices = createAssignmentService()

        private fun cleanup(trxManager: TransactionManager) {
            trxManager.run { trx ->
                trx.requestRepository
                trx.clientRepository
                trx.usersRepository
                trx.locationRepository
            }
        }

        private fun createUserService(transactionManager: TransactionManager) = UserService(transactionManager)

        private fun createCourierService(): CourierService = CourierService(transactionManager)

        private fun createCourierWebSocketHandler(): CourierWebSocketHandler = CourierWebSocketHandler(courierService, userService)

        private fun createAssignmentService(): AssignmentServices = AssignmentServices(transactionManager, courierWebSocketHandler)

        private fun createGeocodingService(): GeocodingServices = GeocodingServices()

        private fun createClientService(): ClientService = ClientService(transactionManager, assignmentServices, geocodingServices)
    }

    private val testAddress =
        Address(
            street = "R. Mário Moreira",
            city = "Odivelas",
            zipCode = "2675-669",
            country = "Portugal",
            streetNumber = "15b",
            floor = "1",
        )

    private val testAddress2 =
        Address(
            street = "R. Diogo Guilherme da Silva Alves Furtado",
            city = "Odivelas",
            zipCode = "2675-642",
            country = "Portugal",
            streetNumber = "8",
            floor = "3",
        )

    private val testUser =
        User(
            email = "client@example.com",
            password = "securePassword123",
            name = "Test Client",
            role = UserRole.CLIENT,
            id = 0,
        )

    @BeforeEach
    fun setup() {
        cleanup(transactionManager)
    }

    @Test
    fun `registerClient should persist user and client`() {
        val testUser2 =
            User(
                email = "client2@example.com",
                password = "securePassword123",
                name = "Test Client",
                role = UserRole.CLIENT,
                id = 0,
            )
        val clientService = createClientService()

        val clientId = clientService.registerClient(testUser2.email, testUser2.password, testUser2.password, testAddress)
        assertIs<Success<Int>>(clientId)

        val savedClient = clientService.getClientById(clientId.value)
        if (savedClient is Either.Right) {
            assertEquals(testUser2.email, savedClient.value.user.email)
        }
    }

    @Test
    fun `makeRequest should create request and requestDetails`() {
        val clientService = createClientService()

        val clientId =
            clientService.registerClient(
                testUser.email,
                testUser.password,
                testUser.name,
                testAddress,
            )
        assertIs<Success<Int>>(clientId)
        val client = clientService.getClientById(clientId.value)!!

        assertIs<Either.Right<Client>>(client)

        transactionManager.run {
            val requestRepo = it.requestRepository

            val requestId =
                runBlocking {
                    clientService.makeRequest(
                        client = client.value,
                        description = "Big Mac",
                        restaurantName = "MC DONALDS Roma",
                        quantity = 1,
                        dropOffAddress = null,
                    )
                }

            assertIs<Success<Int>>(requestId)

            // Validate request
            val allRequests = requestRepo.getAllRequestsForClient(clientId.value)
            assertTrue(allRequests.any { it.id == requestId.value })

            val createdRequest = requestRepo.getRequestById(requestId.value)
            assertNotNull(createdRequest)
            assertEquals(clientId.value, createdRequest.clientId)
            assertEquals("PENDING", createdRequest.requestStatus.status.name)
        }
    }
}
