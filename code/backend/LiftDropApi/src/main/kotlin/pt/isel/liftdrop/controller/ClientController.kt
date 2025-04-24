package pt.isel.liftdrop.controller

import com.example.utils.Either
import com.example.utils.Failure
import com.example.utils.Success
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.liftdrop.Address
import pt.isel.liftdrop.AuthenticatedUser
import pt.isel.liftdrop.model.LoginInputModel
import pt.isel.liftdrop.model.LoginOutputModel
import pt.isel.liftdrop.model.OrderInputModel
import pt.isel.liftdrop.model.RegisterInputModel
import pt.isel.services.ClientError
import pt.isel.services.ClientService

/**
 * Controller for client operations.
 *
 * @param clientService the service to handle client operations
 */
@RequestMapping("/api/client")
@RestController
class ClientController(
    private val clientService: ClientService,
) {
    @PostMapping("/makeOrder")
    fun makeOrder(
        user: AuthenticatedUser,
        @RequestBody order: OrderInputModel,
    ) {
        TODO()
    }

    @GetMapping("/getOrderStatus")
    fun getOrderStatus() {
        TODO()
    }

    @GetMapping("/getETA")
    fun getETA() {
        TODO()
    }

    @PostMapping
    fun registerClient(
        @RequestBody registerInput: RegisterInputModel
    ): ResponseEntity<Any> {
        val register = clientService
            .registerClient(
                registerInput.email,
                registerInput.password,
                registerInput.name,
                Address(
                    0,
                    registerInput.address.country,
                    registerInput.address.city,
                    registerInput.address.street,
                    registerInput.address.streetNumber,
                    registerInput.address.floor,
                    registerInput.address.zipcode,
                )
            )

        return when (register) {
            is Success -> {
                // Handle successful registration
                println("Client registered successfully with ID: ${register.value}")
                ResponseEntity.ok(register.value)
            }
            is Failure -> {
                // Handle registration error
                println("Failed Registration")
                ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email already exists")
            }
        }
    }

    @PostMapping("/login")
    fun login(
        @RequestBody input: LoginInputModel,
    ): ResponseEntity<Any> {
        if (input.email.isBlank() || input.password.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid input")
        }
        val result = clientService
            .loginClient(
                input.email,
                input.password,
            )

        return when (result) {
            is Either.Right -> {
                println("Client logged in successfully with token: ${result.value}")
                val token = result.value
                // Handle successful login
                val cookie =
                    ResponseCookie
                        .from("auth_token", token)
                        .path("/") // Path for which the cookie is valid
                        .maxAge(3600 * 12) // Cookie expiration time
                        .build()
                ResponseEntity
                    .status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(LoginOutputModel(token))
            }
            is Either.Left -> {
                // Handle login error
                println("Failed Login")
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Invalid email or password")
            }
        }

    }
}
