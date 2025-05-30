package pt.isel.liftdrop.controller

import com.example.utils.Either
import com.example.utils.Failure
import com.example.utils.Success
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.liftdrop.Address
import pt.isel.liftdrop.AuthenticatedClient
import pt.isel.liftdrop.Client
import pt.isel.liftdrop.model.AddressInputModel
import pt.isel.liftdrop.model.ClassificationInputModel
import pt.isel.liftdrop.model.LoginInputModel
import pt.isel.liftdrop.model.LoginOutputModel
import pt.isel.liftdrop.model.RegisterClientInputModel
import pt.isel.liftdrop.model.RegisterUserOutput
import pt.isel.liftdrop.model.RequestInputModel
import pt.isel.pipeline.pt.isel.liftdrop.GlobalLogger
import pt.isel.services.client.ClientService
import pt.isel.services.client.RequestCreationError

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
    fun makeRequest(
        user: AuthenticatedClient,
        @RequestBody order: RequestInputModel,
    ): ResponseEntity<Any> =
        when (
            val requestCreationResult =
                clientService.makeRequest(
                    Client(user.client.user),
                    order.itemDesignation,
                    order.restaurantName,
                )
        ) {
            is Success -> {
                val result = requestCreationResult.value
                GlobalLogger.log("Order created successfully with ID: $result")
                ResponseEntity.ok(result)
            }
            is Failure -> {
                // Handle order creation error
                GlobalLogger.log("Failed to create order: ${requestCreationResult.value}")
                when (requestCreationResult.value) {
                    is RequestCreationError.RestaurantNotFound -> {
                        ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body("Restaurant with name '${order.restaurantName}' not found")
                    }
                    is RequestCreationError.ItemNotFound -> {
                        ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body("The item '${order.itemDesignation}' was not found in the restaurant '${order.restaurantName}'")
                    }
                    else ->
                        ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to create order")
                }
            }
        }

    @PostMapping("/register")
    fun registerClient(
        @RequestBody registerInput: RegisterClientInputModel,
    ): ResponseEntity<Any> {
        val register =
            clientService
                .registerClient(
                    registerInput.email,
                    registerInput.password,
                    registerInput.name,
                    Address(
                        registerInput.address.country,
                        registerInput.address.city,
                        registerInput.address.street,
                        registerInput.address.streetNumber,
                        registerInput.address.floor,
                        registerInput.address.zipcode,
                    ),
                )

        return when (register) {
            is Success -> {
                // Handle successful registration
                println("Client registered successfully with ID: ${register.value}")
                ResponseEntity.ok(
                    RegisterUserOutput(
                        register.value.toString(),
                        registerInput.name,
                        registerInput.email,
                        registerInput.password,
                    ),
                )
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
        val result =
            clientService
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

    @DeleteMapping("/logout")
    fun logout(client: AuthenticatedClient): ResponseEntity<Any> {
        val result = clientService.logoutClient(client.token)

        return when (result) {
            is Success -> {
                val expiredCookie =
                    ResponseCookie
                        .from("auth_token", "")
                        .path("/")
                        .maxAge(0) // Expire immediately
                        .build()
                ResponseEntity
                    .status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                    .body("Logout successful")
            }
            is Failure -> {
                println("Failed Logout")
                ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Logout error")
            }
        }
    }

    @PostMapping("/createDropOffLocation")
    fun addDropOffLocation(
        client: AuthenticatedClient,
        @RequestBody address: AddressInputModel,
    ): ResponseEntity<Any> {
        val result =
            clientService
                .addDropOffLocation(
                    client.client.user.id,
                    Address(
                        address.country,
                        address.city,
                        address.street,
                        address.streetNumber,
                        address.floor,
                        address.zipcode,
                    ),
                )

        return when (result) {
            is Success -> {
                println("Drop-off location added successfully with ID: ${result.value}")
                ResponseEntity.ok(result.value)
            }
            is Failure -> {
                println("Failed to add drop-off location")
                ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add drop-off location")
            }
        }
    }

    fun giveClassification(
        client: AuthenticatedClient,
        classification: ClassificationInputModel,
    ): ResponseEntity<Any> {
        val result =
            clientService
                .giveRating(
                    client.client.user.id,
                    classification.requestId,
                    classification.rating,
                )

        return when (result) {
            is Success -> {
                println("Classification given successfully")
                ResponseEntity.ok("Classification given successfully")
            }
            is Failure -> {
                println("Failed to give classification")
                ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to give classification")
            }
        }
    }

    @GetMapping("/hello")
    fun getHello(): ResponseEntity<String> = ResponseEntity.ok("Hello from ClientController!")
}
