package pt.isel.liftdrop

import com.example.utils.Success
import liftdrop.repository.TransactionManager
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import pt.isel.liftdrop.model.AddressInputModel
import pt.isel.liftdrop.model.LoginInputModel
import pt.isel.liftdrop.model.RegisterClientInputModel
import pt.isel.liftdrop.model.RequestInputModel
import pt.isel.services.LocationServices
import pt.isel.services.client.ClientService
import pt.isel.services.utils.Codify.encodePassword
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClientControllerTests {
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
    fun `registerClient should create a new client`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

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
        client
            .post()
            .uri("/client/register")
            .bodyValue(registerClient)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.name")
            .isEqualTo("a")
            .jsonPath("$.email")
            .isEqualTo("a@gmail.com")
            .jsonPath("$.id")
            .isEqualTo("1")
    }

    @Test
    fun `login should return a token`() {
        val client: WebTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

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
        createClient(registerClient)

        // Test successful login
        val validLogin = LoginInputModel(email = "a@gmail.com", password = "password")
        client
            .post()
            .uri("/client/login")
            .bodyValue(validLogin)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("$.token")
            .isNotEmpty
            .jsonPath("$.token")
            .value<String> { token ->
                assertTrue(token.length >= 32, "Token should be at least 32 characters")
            }

        // Test invalid password
        val invalidPassword = LoginInputModel(email = "test@example.com", password = "wrong_password")
        client
            .post()
            .uri("/client/login")
            .bodyValue(invalidPassword)
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `create a dropOff Location should return success`() {
        val client: WebTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

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
        createClient(registerClient)

        val dropOffLocation =
            AddressInputModel(
                street = "R. Bernardim Ribeiro",
                city = "Odivelas",
                country = "Portugal",
                zipcode = "2620-266",
                streetNumber = "5",
                floor = null,
            )

        val token = clientService.loginClient(registerClient.email, registerClient.password)
        assertIs<Success<String>>(token)

        // Create a drop-off location
        client
            .post()
            .uri("/client/createDropOffLocation")
            .cookie("auth_token", token.value)
            .bodyValue(dropOffLocation)
            .exchange()
            .expectStatus()
            .isOk
    }

    @Test
    fun `makeRequest should create a new order`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        assert(true)

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
        createClient(registerClient)

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

        // Make an order
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
            .isAccepted
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
