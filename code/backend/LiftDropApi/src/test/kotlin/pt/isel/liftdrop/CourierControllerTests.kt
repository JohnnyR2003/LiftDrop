package pt.isel.liftdrop

import com.example.utils.Success
import liftdrop.repository.TransactionManager
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import pt.isel.liftdrop.model.AddressInputModel
import pt.isel.liftdrop.model.LoginInputModel
import pt.isel.liftdrop.model.RegisterClientInputModel
import pt.isel.liftdrop.model.RegisterCourierInputModel
import pt.isel.liftdrop.model.RequestInputModel
import pt.isel.services.LocationServices
import pt.isel.services.client.ClientService
import pt.isel.services.courier.CourierService
import pt.isel.services.utils.Codify.encodePassword
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertIs

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
        val responseBody = response.expectBody(String::class.java).returnResult().responseBody
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

        val response =
            courier
                .post()
                .uri("/login")
                .bodyValue(loginCourier)
                .exchange()
                .expectStatus()
                .isOk
    }

    @Test
    fun `test web socket`() {
        // Set up a WebSocket client
        val webSocketClient = StandardWebSocketClient()
        val webSocketHandler = TestWebSocketHandler()

        // Connect to the WebSocket endpoint
        val uri = "ws://localhost:$port/ws/courier"
        val session = webSocketClient.execute(webSocketHandler, uri).get()

        val courier = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api/courier").build()
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

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

        val courierToken = courierService.loginCourier(loginCourier.email, loginCourier.password)
        assertIs<Success<String>>(courierToken)

        val registerClient =
            RegisterClientInputModel(
                name = "a",
                email = "a@gmail.com",
                password = "password",
                address =
                    AddressInputModel(
                        street = "R. Bernardim Ribeiro",
                        city = "Odivelas",
                        country = "Portugal",
                        zipcode = "2620-266",
                        streetNumber = "5",
                        floor = null,
                    ),
            )
        // Register a new client
        createClient(client, registerClient)

        val token = clientService.loginClient(registerClient.email, registerClient.password)
        assertIs<Success<String>>(token)
//      Av. Dom João II 2, 1990-156 Lisboa, Portugal
        locationServices.addPickUpLocation(
            Address(
                country = "Portugal",
                city = "Lisboa",
                street = "Av. Dom João II",
                streetNumber = "2",
                floor = null,
                zipCode = "1990-156",
            ),
            "item",
            "restaurantName",
            10.0,
            10L,
        )

//        courier.post()
//            .uri("/waitingOrders")
//            .bodyValue(StartListeningInputModel(2))
//            .exchange()
//            .expectStatus()
//            .isOk

        val response =
            client
                .post()
                .uri("/client/makeOrder")
                .cookie("auth_token", token.value)
                .bodyValue(
                    RequestInputModel(
                        restaurantName = "restaurantName",
                        itemDesignation = "item",
                    ),
                ).exchange()
                .expectStatus()
                .isOk

//        // Wait and verify message reception
//        assertTrue(webSocketHandler.waitForMessage(5000))  // Wait up to 5 seconds
//        assertNotNull(webSocketHandler.lastMessage)        // Verify message content
    }

    private fun createClient(
        client: WebTestClient,
        registerClient: RegisterClientInputModel,
    ) {
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
