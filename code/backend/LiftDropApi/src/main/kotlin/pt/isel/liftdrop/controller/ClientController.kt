package pt.isel.liftdrop.controller

import com.example.utils.Failure
import com.example.utils.Success
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pt.isel.liftdrop.Address
import pt.isel.liftdrop.AuthenticatedClient
import pt.isel.liftdrop.Client
import pt.isel.liftdrop.Uris
import pt.isel.liftdrop.model.*
import pt.isel.pipeline.pt.isel.liftdrop.GlobalLogger
import pt.isel.services.client.*

/**
 * Controller for client operations.
 *
 * @param clientService the service to handle client operations
 */
@RequestMapping(Uris.Client.BASE)
@RestController
class ClientController(
    private val clientService: ClientService,
) {
    @PostMapping(Uris.Client.MAKE_ORDER)
    fun makeRequest(
        user: AuthenticatedClient,
        @RequestBody order: RequestInputModel,
    ): ResponseEntity<Any> {
        val requestCreationResult =
            clientService.makeRequest(
                client = Client(user.client.user),
                description = order.itemDesignation,
                restaurantName = order.restaurantName,
                quantity = order.quantity,
                dropOffAddress = order.dropOffAddress?.toAddress(),
            )

        return when (requestCreationResult) {
            is Success -> {
                val result = requestCreationResult.value
                GlobalLogger.log("Order created successfully with ID: $result")
                ResponseEntity.ok(result)
            }
            is Failure -> {
                GlobalLogger.log("Failed to create order: ${requestCreationResult.value}")
                val errorResponseMap =
                    mapOf(
                        RequestCreationError.RestaurantNotFound::class to {
                            Problem.restaurantNotFound().response(HttpStatus.NOT_FOUND)
                        },
                        RequestCreationError.ItemNotFound::class to {
                            Problem.itemNotFound().response(HttpStatus.NOT_FOUND)
                        },
                        RequestCreationError.ClientNotFound::class to {
                            Problem.userNotFound().response(HttpStatus.NOT_FOUND)
                        },
                        RequestCreationError.ClientAddressNotFound::class to {
                            Problem.clientAddressNotFound().response(HttpStatus.NOT_FOUND)
                        },
                    )
                errorResponseMap[requestCreationResult.value::class]?.invoke()
                    ?: Problem.internalServerError().response(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    @PostMapping(Uris.Client.REGISTER)
    fun registerClient(
        @RequestBody registerInput: RegisterClientInputModel,
    ): ResponseEntity<Any> {
        val clientCreationResult =
            clientService.registerClient(
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
                val errorResponseMap =
                    mapOf(
                        ClientCreationError.UserAlreadyExists::class to {
                            Problem.userAlreadyExists().response(HttpStatus.CONFLICT)
                        },
                        ClientCreationError.InvalidAddress::class to {
                            Problem.invalidAddress().response(HttpStatus.BAD_REQUEST)
                        },
                        ClientCreationError.InvalidLocation::class to {
                            Problem.invalidLocation().response(HttpStatus.BAD_REQUEST)
                        },
                    )
                errorResponseMap[clientCreationResult.value::class]?.invoke()
                    ?: Problem.internalServerError().response(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    @PostMapping(Uris.Client.LOGIN)
    fun login(
        @RequestBody input: LoginInputModel,
    ): ResponseEntity<Any> {
        val clientLoginResult = clientService.loginClient(input.email, input.password)
        return when (clientLoginResult) {
            is Success -> {
                GlobalLogger.log("Client logged in successfully with token: ${clientLoginResult.value}")
                val token = clientLoginResult.value
                val cookie =
                    ResponseCookie
                        .from("auth_token", token)
                        .path("/")
                        .maxAge(3600 * 12)
                        .build()
                ResponseEntity
                    .status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(ClientLoginOutputModel(token = token))
            }
            is Failure -> {
                val errorResponseMap =
                    mapOf(
                        ClientLoginError.ClientNotFound::class to {
                            Problem.userNotFound().response(HttpStatus.NOT_FOUND)
                        },
                        ClientLoginError.InvalidEmailOrPassword::class to {
                            Problem.invalidRequestContent("Invalid email or password").response(HttpStatus.BAD_REQUEST)
                        },
                        ClientLoginError.BlankEmailOrPassword::class to {
                            Problem.invalidRequestContent("Email and password cannot be blank").response(HttpStatus.BAD_REQUEST)
                        },
                        ClientLoginError.WrongPassword::class to {
                            Problem.passwordIsIncorrect().response(HttpStatus.UNAUTHORIZED)
                        },
                        ClientLoginError.ClientLoginEmailAlreadyExists::class to {
                            Problem.userAlreadyExists().response(HttpStatus.CONFLICT)
                        },
                        ClientLoginError.InvalidAddress::class to {
                            Problem.invalidAddress().response(HttpStatus.BAD_REQUEST)
                        },
                    )
                errorResponseMap[clientLoginResult.value::class]?.invoke()
                    ?: Problem.internalServerError().response(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    @DeleteMapping(Uris.Client.LOGOUT)
    fun logout(client: AuthenticatedClient): ResponseEntity<Any> =
        when (val clientLogoutResult = clientService.logoutClient(client.token)) {
            is Success -> {
                val expiredCookie =
                    ResponseCookie
                        .from("auth_token", "")
                        .path("/")
                        .maxAge(0)
                        .build()
                ResponseEntity
                    .status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                    .body("Logout successful")
            }
            is Failure -> {
                val errorResponseMap =
                    mapOf(
                        ClientLogoutError.SessionNotFound::class to {
                            Problem.sessionNotFound().response(HttpStatus.NOT_FOUND)
                        },
                    )
                errorResponseMap[clientLogoutResult.value::class]?.invoke()
                    ?: Problem.internalServerError().response(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }

    @PostMapping(Uris.Client.CREATE_DROP_OFF_LOCATION)
    fun addDropOffLocation(
        client: AuthenticatedClient,
        @RequestBody address: AddressInputModel,
    ): ResponseEntity<Any> {
        val result =
            clientService.addDropOffLocation(
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
                val errorResponseMap =
                    mapOf(
                        DropOffCreationError.ClientNotFound::class to {
                            Problem.userNotFound().response(HttpStatus.NOT_FOUND)
                        },
                        DropOffCreationError.InvalidAddress::class to {
                            Problem.invalidAddress().response(HttpStatus.BAD_REQUEST)
                        },
                    )
                errorResponseMap[result.value::class]?.invoke()
                    ?: Problem.internalServerError().response(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    @GetMapping(Uris.Client.GET_REQUEST_STATUS)
    fun getRequestStatus(
        client: AuthenticatedClient,
        @PathVariable requestId: Int,
    ): ResponseEntity<Any> =
        when (val result = clientService.getRequestStatus(client.client.user.id, requestId)) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> {
                val errorResponseMap =
                    mapOf(
                        ClientGetRequestStatusError.RequestNotFound::class to {
                            Problem.requestNotFound().response(HttpStatus.NOT_FOUND)
                        },
                    )
                errorResponseMap[result.value::class]?.invoke()
                    ?: Problem.internalServerError().response(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }

    @PostMapping(Uris.Client.GIVE_CLASSIFICATION)
    fun giveClassification(
        client: AuthenticatedClient,
        @RequestBody classification: ClassificationInputModel,
    ): ResponseEntity<Any> {
        val result =
            clientService.giveRating(
                client.client.user.id,
                classification.rating,
            )
        return when (result) {
            is Success -> {
                println("Classification given successfully")
                ResponseEntity.ok("Classification given successfully")
            }
            is Failure -> {
                val errorResponseMap =
                    mapOf(
                        ClientRatingError.RequestNotFound::class to {
                            Problem.requestNotFound().response(HttpStatus.NOT_FOUND)
                        },
                        ClientRatingError.RatingAlreadyDone::class to {
                            Problem.ratingAlreadyDone().response(HttpStatus.BAD_REQUEST)
                        },
                    )
                errorResponseMap[result.value::class]?.invoke()
                    ?: Problem.internalServerError().response(HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    @GetMapping(Uris.Client.HELLO)
    fun getHello(): ResponseEntity<String> = ResponseEntity.ok("Hello from ClientController!")
}
