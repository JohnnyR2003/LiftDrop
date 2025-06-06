package pt.isel.liftdrop.controller

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
import pt.isel.liftdrop.model.*
import pt.isel.pipeline.pt.isel.liftdrop.GlobalLogger
import pt.isel.services.client.ClientCreationError
import pt.isel.services.client.ClientLoginError
import pt.isel.services.client.ClientLogoutError
import pt.isel.services.client.ClientService
import pt.isel.services.client.DropOffCreationError
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
                        Problem.RestaurantNotFound
                            .response(HttpStatus.NOT_FOUND)
                    }
                    is RequestCreationError.ItemNotFound -> {
                        Problem.ItemNotFound
                            .response(HttpStatus.NOT_FOUND)
                    }
                    is RequestCreationError.ClientNotFound -> {
                        Problem.UserNotFound
                            .response(HttpStatus.NOT_FOUND)
                    }
                    is RequestCreationError.ClientAddressNotFound -> {
                        Problem.ClientAddressNotFound
                            .response(HttpStatus.NOT_FOUND)
                    }
                    else ->
                        Problem.InternalServerError
                            .response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
            }
        }

    @PostMapping("/register")
    fun registerClient(
        @RequestBody registerInput: RegisterClientInputModel,
    ): ResponseEntity<Any> {
        val clientCreationResult =
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

        return when (clientCreationResult) {
            is Success -> {
                ResponseEntity.ok(
                    RegisterUserOutput(
                        clientCreationResult.value.toString(),
                        registerInput.name,
                        registerInput.email,
                        registerInput.password,
                    ),
                )
            }
            is Failure -> {
                when (clientCreationResult.value) {
                    is ClientCreationError.UserAlreadyExists -> {
                        Problem.UserAlreadyExists
                            .response(HttpStatus.CONFLICT)
                    }
                    is ClientCreationError.InvalidAddress -> {
                        Problem.InvalidAddress
                            .response(HttpStatus.BAD_REQUEST)
                    }
                    is ClientCreationError.InvalidLocation -> {
                        Problem.InvalidLocation
                            .response(HttpStatus.BAD_REQUEST)
                    }
                    else -> {
                        Problem.InternalServerError
                            .response(HttpStatus.INTERNAL_SERVER_ERROR)
                    }
                }
            }
        }
    }

    @PostMapping("/login")
    fun login(
        @RequestBody input: LoginInputModel,
    ): ResponseEntity<Any> {
        val clientLoginResult =
            clientService
                .loginClient(
                    input.email,
                    input.password,
                )
        return when (clientLoginResult) {
            is Success -> {
                GlobalLogger.log("Client logged in successfully with token: ${clientLoginResult.value}")
                val token = clientLoginResult.value
                val cookie =
                    ResponseCookie
                        .from("auth_token", token)
                        .path("/") // Path for which the cookie is valid
                        .maxAge(3600 * 12) // Cookie expiration time
                        .build()
                ResponseEntity
                    .status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(ClientLoginOutputModel(token = token))
            }
            is Failure -> {
                when (clientLoginResult.value) {
                    is ClientLoginError.ClientNotFound -> {
                        Problem.UserNotFound
                            .response(HttpStatus.NOT_FOUND)
                    }
                    is ClientLoginError.InvalidEmailOrPassword -> {
                        Problem.InvalidRequestContent
                            .response(HttpStatus.BAD_REQUEST)
                    }
                    is ClientLoginError.BlankEmailOrPassword -> {
                        Problem.InvalidRequestContent
                            .response(HttpStatus.BAD_REQUEST)
                    }
                    is ClientLoginError.WrongPassword -> {
                        Problem.PasswordIsIncorrect
                            .response(HttpStatus.UNAUTHORIZED)
                    }
                    is ClientLoginError.ClientLoginEmailAlreadyExists -> {
                        Problem.UserAlreadyExists
                            .response(HttpStatus.CONFLICT)
                    }
                    is ClientLoginError.InvalidAddress -> {
                        Problem.InvalidAddress
                            .response(HttpStatus.BAD_REQUEST)
                    }
                    else -> {
                        Problem.InternalServerError
                            .response(HttpStatus.INTERNAL_SERVER_ERROR)
                    }
                }
            }
        }
    }

    @DeleteMapping("/logout")
    fun logout(client: AuthenticatedClient): ResponseEntity<Any> =
        when (val clientLogoutResult = clientService.logoutClient(client.token)) {
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
                when (clientLogoutResult.value) {
                    is ClientLogoutError.SessionNotFound -> {
                        Problem.SessionNotFound
                            .response(HttpStatus.NOT_FOUND)
                    }
                    else -> {
                        Problem.InternalServerError
                            .response(HttpStatus.INTERNAL_SERVER_ERROR)
                    }
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
                when (result.value) {
                    is DropOffCreationError.ClientNotFound -> {
                        Problem.UserNotFound
                            .response(HttpStatus.NOT_FOUND)
                    }

                    is DropOffCreationError.InvalidAddress -> {
                        Problem.InvalidAddress
                            .response(HttpStatus.BAD_REQUEST)
                    }
                    else -> {
                        Problem.InternalServerError
                            .response(HttpStatus.INTERNAL_SERVER_ERROR)
                    }
                }
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
