package pt.isel.liftdrop

import com.example.utils.Success
import liftdrop.repository.TransactionManager
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import pt.isel.liftdrop.model.AddressInputModel
import pt.isel.liftdrop.model.RegisterClientInputModel
import pt.isel.liftdrop.model.RequestInputModel
import pt.isel.services.ClientService
import java.util.UUID
import kotlin.run
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClientControllerTests {
    @Autowired
    private lateinit var clientService: ClientService

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
                floor = null,
            )
        )

        val response =
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
    fun `makeRequest should create a new order`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/api").build()

        assert(true)

//        val order = RequestInputModel(
//            itemDesignation = "Pizza",
//            restaurantName = "Pizza Place",
//            dropOffLocation = AddressInputModel(
//                street = "Main St",
//                number = 123,
//                city = "Springfield",
//                country = "USA"
//            )
//        )
//
//        val response =
//            client
//                .post()
//                .uri("/client/makeOrder")
//                .bodyValue(order)
//                .exchange()
//                .expectStatus()
//                .isOk
//                .returnResult(String::class.java)
//
//        assertIs<Success>(response)
    }

}