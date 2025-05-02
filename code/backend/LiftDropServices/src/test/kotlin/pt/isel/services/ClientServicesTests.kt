package pt.isel.services

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
import pt.isel.pipeline.pt.isel.liftdrop.LocationDTO
import pt.isel.services.google.GeocodingServices
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

        private val courierService = createCourierService()

        private val courierWebSocketHandler = createCourierWebSocketHandler()

        private val geocodingServices = createGeocodingService()

        private fun cleanup(trxManager: TransactionManager) {
            trxManager.run { trx ->
                trx.requestRepository
                trx.clientRepository
                trx.usersRepository
                trx.locationRepository
            }
        }

        private fun createCourierService(): CourierService = CourierService(transactionManager)

        private fun createCourierWebSocketHandler(): CourierWebSocketHandler = CourierWebSocketHandler(courierService)

        private fun createGeocodingService(): GeocodingServices = GeocodingServices(transactionManager, courierWebSocketHandler)

        private fun createClientService(): ClientService = ClientService(transactionManager, geocodingServices)
    }

    private val testAddress =
        Address(
            street = "123 Test St",
            city = "Testville",
            zipCode = "1",
            country = "Testland",
            streetNumber = "1A",
            floor = "1",
        )

    private val testAddress2 =
        Address(
            street = "1234 Test2 St",
            city = "Testville",
            zipCode = "2",
            country = "Testland",
            streetNumber = "2A",
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
            val locationRepo = it.locationRepository
            val requestRepo = it.requestRepository

            val pickupLocationId =
                locationRepo.createLocation(
                    LocationDTO(38.7169, -9.1399),
                    testAddress,
                )
            val dropoffLocationId =
                locationRepo.createLocation(
                    LocationDTO(40.4168, -3.7038),
                    testAddress2,
                )

            val pickup = locationRepo.getLocationById(pickupLocationId)

            val dropoff = locationRepo.getLocationById(dropoffLocationId)

            val requestId =
                runBlocking {
                    clientService.makeRequest(
                        client = client.value,
                        description = "Send package please",
                        pickupLocation = pickup,
                        dropOffLocation = dropoff,
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
