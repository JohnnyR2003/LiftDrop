package pt.isel.liftdrop

import com.example.utils.Either
import com.example.utils.Success
import liftdrop.repository.TransactionManager
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.socket.*
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import pt.isel.liftdrop.model.*
import pt.isel.services.LocationServices
import pt.isel.services.client.ClientService
import pt.isel.services.courier.CourierService
import pt.isel.services.utils.Codify.encodePassword
import java.net.URI
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertIs
import org.springframework.web.socket.WebSocketHttpHeaders
import pt.isel.services.courier.UserDetails
import kotlin.test.assertNotNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CourierControllerTests {
    @Autowired
    private lateinit var courierService: CourierService

    @Autowired
    private lateinit var clientService: ClientService

    @Autowired
    private lateinit var locationServices: LocationServices

    @LocalServerPort
    var port: Int = 0

    @Autowired
    private lateinit var trxManager: TransactionManager

    @BeforeEach
    fun setUp() {
        trxManager.run {
            println("Clearing database...")
            it.requestRepository.clear()
            it.clientRepository.clear()
            it.courierRepository.clear()
            it.locationRepository.clear()
            it.usersRepository.clear()
        }
    }

    @Test
    fun `register with courier`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api/courier").build()

        val registerCourier =
            RegisterCourierInputModel(
                name = "b",
                email = "courier@gmail.com",
                password = "randomPassword",
            )

        val response =
            client
                .post()
                .uri("/register")
                .bodyValue(registerCourier)
                .exchange()
                .expectStatus()
                .isOk
        response.expectBody(String::class.java).returnResult().responseBody
    }

    @Test
    fun `login with courier`() {
        val courier = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api/courier").build()

        val registerCourier =
            RegisterCourierInputModel(
                name = "b",
                email = "courier@gmail.com",
                password = "randomPassword",
            )

        courier
            .post()
            .uri("/register")
            .bodyValue(registerCourier)
            .exchange()
            .expectStatus()
            .isOk

        val loginCourier =
            LoginInputModel(
                email = "courier@gmail.com",
                password = "randomPassword",
            )

        courier
            .post()
            .uri("/login")
            .bodyValue(loginCourier)
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `test WebSocket pickup and dropOff`() {
        // Setup WebTestClients
        val courierWebTest = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api/courier").build()

        // ─────────────────────────────────────────────────────────────
        // STEP 1: Register and login courier
        // ─────────────────────────────────────────────────────────────
        val registerCourier = RegisterCourierInputModel(
            name = "b",
            email = "courier@gmail.com",
            password = "randomPassword"
        )

        courierWebTest.post()
            .uri("/register")
            .bodyValue(registerCourier)
            .exchange()
            .expectStatus().isOk

        val loginCourier = LoginInputModel(email = "courier@gmail.com", password = "randomPassword")
        val courier = courierService.loginCourier(loginCourier.email, loginCourier.password)
        assertIs<Success<UserDetails>>(courier)
        val courierInfo = courier.value

        // ─────────────────────────────────────────────────────────────
        // STEP 2: Connect courier to WebSocket
        // ─────────────────────────────────────────────────────────────
        val handler = TestWebSocketHandler()
        val webSocketClient = StandardWebSocketClient()
        val headers = WebSocketHttpHeaders().apply {
            add("Authorization", "Bearer ${courierInfo.token}")
        }
        val uri = URI("ws://localhost:$port/ws/courier")
        webSocketClient.execute(handler, headers, uri).get()

        // ─────────────────────────────────────────────────────────────
        // STEP 3: Register and login client
        // ─────────────────────────────────────────────────────────────
        val registerClient = RegisterClientInputModel(
            name = "a",
            email = "a@gmail.com",
            password = "password",
            address = AddressInputModel(
                street = "R. Bernardim Ribeiro",
                city = "Odivelas",
                country = "Portugal",
                zipcode = "2620-266",
                streetNumber = "5",
                floor = null
            )
        )
        val clientId = createClient(registerClient)

        val clientTokenResult = clientService.loginClient(registerClient.email, registerClient.password)
        assertIs<Success<String>>(clientTokenResult)

        // ─────────────────────────────────────────────────────────────
        // STEP 4: Update courier location
        // ─────────────────────────────────────────────────────────────
        courierWebTest.post()
            .uri("/updateLocation")
            .cookie("auth_token", courierInfo.token)
            .bodyValue(
                LocationUpdateInputModel(
                    courier.value.courierId,
                    LocationDTO(38.73538, -9.145238)
                )
            )
            .exchange()
            .expectStatus().isOk

        // ─────────────────────────────────────────────────────────────
        // STEP 7: Toggle Availability
        // ─────────────────────────────────────────────────────────────

        handler.sendMessage("{\"type\": \"READY\"}")

        // ─────────────────────────────────────────────────────────────
        // STEP 5: Add pickup location
        // ─────────────────────────────────────────────────────────────
        locationServices.addPickUpLocation(
            Address(
                country = "PORTUGAL",
                city = "Lisbon",
                street = "Avenida da República",
                streetNumber = "12",
                floor = null,
                zipCode = "1050-191"
            ),
            "Big Mac",
            "MC DONALDS Saldanha",
            price = 10.0,
            eta = 15L
        )

        // ─────────────────────────────────────────────────────────────
        // STEP 6: Client places order
        // ─────────────────────────────────────────────────────────────

        println("Client ID: $clientId")
        val client =
            clientService.getClientById(clientId)
        assertIs<Either.Right<Client>>(client)


        val request =
            clientService.makeRequest(
                client = client.value,
                description = "Big Mac",
                restaurantName = "MC DONALDS Saldanha",
                quantity = 1,
                dropOffAddress = null,
            )

        assertIs<Success<MakeRequestReturn>>(request)

        // ─────────────────────────────────────────────────────────────
        // STEP 8: Assert WebSocket received order
        // ─────────────────────────────────────────────────────────────
        assertTrue(handler.waitForMessage(5000))
        assertNotNull(handler.lastMessage)

        // ─────────────────────────────────────────────────────────────
        // STEP 9: Accept the order
        // ─────────────────────────────────────────────────────────────
        handler.sendMessage(
            json = "{\"type\": \"DECISION\", " +
                    "\"requestId\": ${request.value.requestId}, " +
                    "\"decision\": \"ACCEPT\"}"
        )

        // ─────────────────────────────────────────────────────────────
        // STEP 10: Simulate pickup
        // ─────────────────────────────────────────────────────────────

        courierWebTest
            .post()
            .uri("/pickedUpOrder")
            .cookie("auth_token", courierInfo.token)
            .bodyValue(
                PickupOrderInputModel(request.value.requestId, courierInfo.courierId, request.value.pickupCode, "DEFAULT")
            )
            .exchange()
            .expectStatus()
            .isOk

        // ─────────────────────────────────────────────────────────────
        // STEP 11: Update courier location to drop-off
        // ─────────────────────────────────────────────────────────────

        courierWebTest.post()
            .uri("/updateLocation")
            .cookie("auth_token", courierInfo.token)
            .bodyValue(
                LocationUpdateInputModel(
                    courierInfo.courierId,
                    LocationDTO(38.725985,-9.143897)
                )
            )
            .exchange()
            .expectStatus()
            .isOk

        // ─────────────────────────────────────────────────────────────
        // STEP 12: Simulate drop-off
        // ─────────────────────────────────────────────────────────────

        courierWebTest.post()
            .uri("/deliveredOrder")
            .cookie("auth_token", courierInfo.token)
            .bodyValue(
                DeliverOrderInputModel(
                    request.value.requestId,
                    courierInfo.courierId,
                    request.value.dropOffCode,
                    5.6
                )
            )
            .exchange()
            .expectStatus()
            .isOk
    }


    @Test
    fun `testing decline and cancel order by which couriers are chosen`() {
        // Setup WebTestClients
        val courierWebTest = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api/courier").build()

        // ─────────────────────────────────────────────────────────────
        // STEP 1: Register and login courier 1
        // ─────────────────────────────────────────────────────────────
        val registerCourier = RegisterCourierInputModel(
            name = "b",
            email = "courier@gmail.com",
            password = "randomPassword"
        )

        courierWebTest.post()
            .uri("/register")
            .bodyValue(registerCourier)
            .exchange()
            .expectStatus().isOk

        val loginCourier = LoginInputModel(email = "courier@gmail.com", password = "randomPassword")
        val courier = courierService.loginCourier(loginCourier.email, loginCourier.password)
        assertIs<Success<UserDetails>>(courier)
        val courierInfo = courier.value

        // ─────────────────────────────────────────────────────────────
        // STEP 2: Register and login courier 2
        // ─────────────────────────────────────────────────────────────
        val registerCourier2 = RegisterCourierInputModel(
            name = "c",
            email = "courier2@gmail.com",
            password = "randomPassword"
        )

        courierWebTest.post()
            .uri("/register")
            .bodyValue(registerCourier2)
            .exchange()
            .expectStatus().isOk

        val loginCourier2 = LoginInputModel(email = registerCourier2.email, password = registerCourier2.password)
        val courier2 = courierService.loginCourier(loginCourier2.email, loginCourier2.password)
        assertIs<Success<UserDetails>>(courier2)
        val courierInfo2 = courier2.value

        // ─────────────────────────────────────────────────────────────
        // STEP 3: Connect courier 1 to WebSocket
        // ─────────────────────────────────────────────────────────────
        val handler = TestWebSocketHandler()
        val webSocketClient = StandardWebSocketClient()
        val headers = WebSocketHttpHeaders().apply {
            add("Authorization", "Bearer ${courierInfo.token}")
        }
        val uri = URI("ws://localhost:$port/ws/courier")
        webSocketClient.execute(handler, headers, uri).get()

        // ─────────────────────────────────────────────────────────────
        // STEP 4: Connect courier 2 to WebSocket
        // ─────────────────────────────────────────────────────────────
        val handler2 = TestWebSocketHandler()
        val webSocketClient2 = StandardWebSocketClient()
        val headers2 = WebSocketHttpHeaders().apply {
            add("Authorization", "Bearer ${courierInfo2.token}")
        }
        val uri2 = URI("ws://localhost:$port/ws/courier")
        webSocketClient2.execute(handler2, headers2, uri2).get()

        // ─────────────────────────────────────────────────────────────
        // STEP 5: Register and login client
        // ─────────────────────────────────────────────────────────────
        val registerClient = RegisterClientInputModel(
            name = "a",
            email = "a@gmail.com",
            password = "password",
            address = AddressInputModel(
                street = "R. Bernardim Ribeiro",
                city = "Odivelas",
                country = "Portugal",
                zipcode = "2620-266",
                streetNumber = "5",
                floor = null
            )
        )
        val clientId = createClient(registerClient)

        val clientTokenResult = clientService.loginClient(registerClient.email, registerClient.password)
        assertIs<Success<String>>(clientTokenResult)


        // ─────────────────────────────────────────────────────────────
        // STEP 6: Update courier 1 location
        // ─────────────────────────────────────────────────────────────
        courierWebTest.post()
            .uri("/updateLocation")
            .cookie("auth_token", courierInfo.token)
            .bodyValue(
                LocationUpdateInputModel(
                    courier.value.courierId,
                    LocationDTO(38.73538, -9.145238)
                )
            )
            .exchange()
            .expectStatus().isOk

        // ─────────────────────────────────────────────────────────────
        // STEP 7: Update courier 2 location
        // ─────────────────────────────────────────────────────────────
        courierWebTest.post()
            .uri("/updateLocation")
            .cookie("auth_token", courierInfo2.token)
            .bodyValue(
                LocationUpdateInputModel(
                    courier2.value.courierId,
                    LocationDTO(38.734573,-9.144732)
                )
            )
            .exchange()
            .expectStatus().isOk

        // ─────────────────────────────────────────────────────────────
        // STEP 8: Toggle Availability of courier 1
        // ─────────────────────────────────────────────────────────────

        handler.sendMessage("{\"type\": \"READY\"}")

        // ─────────────────────────────────────────────────────────────
        // STEP 9: Toggle Availability of courier 2
        // ─────────────────────────────────────────────────────────────

        handler2.sendMessage("{\"type\": \"READY\"}")

        // ─────────────────────────────────────────────────────────────
        // STEP 10: Add pickup location
        // ─────────────────────────────────────────────────────────────
        locationServices.addPickUpLocation(
            Address(
                country = "PORTUGAL",
                city = "Lisbon",
                street = "Avenida da República",
                streetNumber = "12",
                floor = null,
                zipCode = "1050-191"
            ),
            "Big Mac",
            "MC DONALDS Saldanha",
            price = 10.0,
            eta = 15L
        )

        // ─────────────────────────────────────────────────────────────
        // STEP 11: Client places order
        // ─────────────────────────────────────────────────────────────

        val client =
            clientService.getClientById(clientId)
        assertIs<Either.Right<Client>>(client)


        val request =
            clientService.makeRequest(
                client = client.value,
                description = "Big Mac",
                restaurantName = "MC DONALDS Saldanha",
                quantity = 1,
                dropOffAddress = null,
            )

        assertIs<Success<MakeRequestReturn>>(request)

        // ─────────────────────────────────────────────────────────────
        // STEP 12: Assert WebSocket received order
        // ─────────────────────────────────────────────────────────────
        assertTrue(handler.waitForMessage(5000))
        assertNotNull(handler.lastMessage)

        // ─────────────────────────────────────────────────────────────
        // STEP 13: Courier 1 declines the order
        // ─────────────────────────────────────────────────────────────

        handler.sendMessage(
            json = "{\"type\": \"DECISION\", " +
                    "\"requestId\": ${request.value.requestId}, " +
                    "\"decision\": \"DECLINE\"}"
        )

        // ─────────────────────────────────────────────────────────────
        // STEP 14: Courier 2 receives the order
        // ─────────────────────────────────────────────────────────────

        assertTrue(handler2.waitForMessage(5000))
        assertNotNull(handler2.lastMessage)

        // ─────────────────────────────────────────────────────────────
        // STEP 15: Courier 2 accepts the order
        // ─────────────────────────────────────────────────────────────
        handler2.sendMessage(
            json = "{\"type\": \"DECISION\", " +
                    "\"requestId\": ${request.value.requestId}, " +
                    "\"decision\": \"ACCEPT\"}"
        )

        // ─────────────────────────────────────────────────────────────
        // STEP 16: Simulate pickup by courier 2
        // ─────────────────────────────────────────────────────────────

        courierWebTest
            .post()
            .uri("/pickedUpOrder")
            .cookie("auth_token", courierInfo2.token)
            .bodyValue(
                PickupOrderInputModel(request.value.requestId, courierInfo2.courierId, request.value.pickupCode, "DEFAULT")
            )
            .exchange()
            .expectStatus()
            .isOk

        // ─────────────────────────────────────────────────────────────
        // STEP 17: Courier 2 cancels the order
        // ─────────────────────────────────────────────────────────────

        courierWebTest.post()
            .uri("/cancelDelivery")
            .cookie("auth_token", courierInfo2.token)
            .bodyValue(
                CancelDeliveryInputModel(
                    courierInfo2.courierId,
                    request.value.requestId,
                    "HEADING_TO_DROPOFF",
                    LocationDTO(38.734573,-9.144732)
                )
            )
            .exchange()
            .expectStatus()
            .isOk
    }




    private fun createClient(registerClient: RegisterClientInputModel): Int = trxManager.run {
            val userId =
                it.usersRepository.createUser(
                    registerClient.email,
                    registerClient.password.encodePassword(),
                    registerClient.name,
                    UserRole.CLIENT,
                )
            it.clientRepository.createClient(
                userId,
                Address(
                    registerClient.address.country,
                    registerClient.address.city,
                    registerClient.address.street,
                    registerClient.address.streetNumber,
                    registerClient.address.floor,
                    registerClient.address.zipcode,
                ),
            )
            val locId =
                it.locationRepository.createLocation(
                    LocationDTO(38.80694, -9.189583),
                    Address(
                        registerClient.address.country,
                        registerClient.address.city,
                        registerClient.address.street,
                        registerClient.address.streetNumber,
                        registerClient.address.floor,
                        registerClient.address.zipcode,
                    ),
                )
            it.locationRepository.createDropOffLocation(
                userId,
                locId,
            )
            return@run userId
        }
}

class TestWebSocketHandler : WebSocketHandler {
    private val messageReceived = CountDownLatch(1)
    private var session: WebSocketSession? = null
    var lastMessage: String? = null

    override fun afterConnectionEstablished(session: WebSocketSession) {
        println("✅ Connection established: ${session.id}")
        this.session = session
    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        lastMessage = message.payload.toString()
        messageReceived.countDown()
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        println("WebSocket transport error: ${exception.message}")
        exception.printStackTrace()
        messageReceived.countDown()
    }

    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        println("WebSocket connection closed: ${session.id}, status: $closeStatus")
        messageReceived.countDown()
    }

    override fun supportsPartialMessages(): Boolean = false

    fun waitForMessage(timeoutMs: Long): Boolean =
        messageReceived.await(timeoutMs, TimeUnit.MILLISECONDS)

    fun sendMessage(json: String) {
        session?.sendMessage(TextMessage(json))
            ?: throw IllegalStateException("WebSocket session not established")
    }
}

