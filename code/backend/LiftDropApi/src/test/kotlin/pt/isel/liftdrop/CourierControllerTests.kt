package pt.isel.liftdrop

import com.example.utils.Success
import liftdrop.repository.TransactionManager
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
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
import kotlin.test.assertNotNull
import org.springframework.web.socket.WebSocketHttpHeaders
import pt.isel.services.courier.UserDetails

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
    fun `test WebSocket order dispatch flow`() {
        val courierWebTest = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api/courier").build()
        val clientWebTest = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api/client").build()

        // Step 1: Register and login a courier
        val registerCourier = RegisterCourierInputModel(
            name = "b",
            email = "courier@gmail.com",
            password = "randomPassword"
        )
        courierWebTest
            .post()
            .uri("/register")
            .bodyValue(registerCourier)
            .exchange()
            .expectStatus().isOk

        val loginCourier = LoginInputModel(email = "courier@gmail.com", password = "randomPassword")
        val courierTokenResult = courierService.loginCourier(loginCourier.email, loginCourier.password)
        assertIs<Success<UserDetails>>(courierTokenResult)
        val courierToken = courierTokenResult.value

        // Step 2: Establish WebSocket connection with Authorization header
        val handler = TestWebSocketHandler() // single instance used for listening & assertions
        val webSocketClient = StandardWebSocketClient()

        val headers = WebSocketHttpHeaders()
        headers.add("Authorization", "Bearer $courierToken")

        val uri = URI("ws://localhost:$port/ws/courier")

        webSocketClient.execute(handler, headers, uri).get()

        // Step 4: Register a client and place an order
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
        createClient(registerClient)

        val clientTokenResult = clientService.loginClient(registerClient.email, registerClient.password)
        assertIs<Success<String>>(clientTokenResult)
        val clientToken = clientTokenResult.value

        courierWebTest
            .post()
            .uri("/updateLocation")
            .cookie("auth_token", courierToken.token)
            .bodyValue(
                LocationUpdateInputModel(
                    courierTokenResult.value.courierId,
                    LocationDTO(38.73538,-9.145238), // Example coordinates
                )
            )
            .exchange()
            .expectStatus().isOk

        println("Courier location updated")

        //Create a pickup location
        locationServices
            .addPickUpLocation(
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

        clientWebTest
            .post()
            .uri("/makeOrder")
            .cookie("auth_token", clientToken)
            .bodyValue(RequestInputModel("MC DONALDS Saldanha", "Big Mac"))
            .exchange()
            .expectStatus().isOk

        println("Order placed successfully")

        // Step 5: Wait for the message sent to the courier via WebSocket
        assertTrue(handler.waitForMessage(5000))
//        assertNotNull(handler.lastMessage)
        println("WebSocket received: ${handler.lastMessage}")
    }


    private fun createClient(registerClient: RegisterClientInputModel) {
        trxManager.run {
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
        }
    }
}

class TestWebSocketHandler : WebSocketHandler {
    private val messageReceived = CountDownLatch(1) // Tracks message receipt
    var lastMessage: String? = null // Stores the received message

    override fun afterConnectionEstablished(session: WebSocketSession) {
        // This method is called when the WebSocket connection is established
        println("WebSocket connection established: ${session.id}")
    }

    override fun handleMessage(
        session: WebSocketSession,
        message: WebSocketMessage<*>,
    ) {
        println("WebSocket message received: ${message.payload}")
        lastMessage = message.payload.toString()
        messageReceived.countDown()
    }

    override fun handleTransportError(
        session: WebSocketSession,
        exception: Throwable,
    ) {
        println("WebSocket transport error: ${exception.message}")
        exception.printStackTrace()
        messageReceived.countDown() // Ensure the latch is decremented even on error
    }

    override fun afterConnectionClosed(
        session: WebSocketSession,
        closeStatus: CloseStatus,
    ) {
        println("WebSocket connection closed: ${session.id}, status: $closeStatus")
        messageReceived.countDown() // Ensure the latch is decremented when the connection closes
    }

    override fun supportsPartialMessages(): Boolean {
        return false // This handler does not support partial messages
    }

    fun waitForMessage(timeoutMs: Long): Boolean = messageReceived.await(timeoutMs, TimeUnit.MILLISECONDS)
}
