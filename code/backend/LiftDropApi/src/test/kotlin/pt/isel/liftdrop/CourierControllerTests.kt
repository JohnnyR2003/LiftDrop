package pt.isel.liftdrop

import liftdrop.repository.TransactionManager
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.web.reactive.server.WebTestClient
import pt.isel.liftdrop.model.LoginInputModel
import pt.isel.liftdrop.model.RegisterCourierInputModel
import pt.isel.services.ClientService
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CourierControllerTests {

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
    fun `register with courier`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/courier").build()

        val registerCourier = RegisterCourierInputModel(
            name = "b",
            email = "courier@gmail.com",
            password = "randomPassword",
        )

        val response = client.post()
            .uri("/register")
            .bodyValue(registerCourier)
            .exchange()
            .expectStatus()
            .isOk
        val responseBody = response.expectBody(String::class.java).returnResult().responseBody
    }

    @Test
    fun `login with courier`() {
        val client = WebTestClient.bindToServer().baseUrl("http://localhost:$port/courier").build()


        val registerCourier = RegisterCourierInputModel(
            name = "b",
            email = "courier@gmail.com",
            password = "randomPassword",
        )

        client.post()
            .uri("/register")
            .bodyValue(registerCourier)
            .exchange()
            .expectStatus()
            .isOk

        val loginCourier = LoginInputModel(
            email = "courier@gmail.com",
            password = "randomPassword",
        )

        val response = client.post()
            .uri("/login")
            .bodyValue(loginCourier)
            .exchange()
            .expectStatus()
            .isOk
    }

}