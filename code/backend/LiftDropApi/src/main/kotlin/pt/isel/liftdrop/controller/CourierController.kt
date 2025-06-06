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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.liftdrop.model.*
import pt.isel.pipeline.pt.isel.liftdrop.GlobalLogger
import pt.isel.services.courier.*
import pt.isel.services.google.GeocodingServices

@RestController
@RequestMapping("/courier")
class CourierController(
    val courierService: CourierService,
    val geocodingServices: GeocodingServices,
) {
    @PostMapping("/register")
    fun registerCourier(
        @RequestBody registerInput: RegisterCourierInputModel,
    ): ResponseEntity<Any> {
        val courierCreationResult =
            courierService
                .registerCourier(
                    email = registerInput.email,
                    password = registerInput.password,
                    name = registerInput.name,
                )

        return when (courierCreationResult) {
            is Success -> {
                ResponseEntity.ok(RegisterCourierOutputModel(courierCreationResult.value))
            }

            is Failure -> {
                when (courierCreationResult.value) {
                    is CourierCreationError.CourierEmailAlreadyExists -> {
                        Problem.CourierAlreadyExists.response(HttpStatus.CONFLICT)
                    }
                    else -> {
                        Problem.InternalServerError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                    }
                }
            }
        }
    }

    @PostMapping("/login")
    fun login(
        @RequestBody input: LoginInputModel,
    ): ResponseEntity<Any> {
        val courierLoginResult =
            courierService
                .loginCourier(
                    input.email,
                    input.password,
                )

        return when (courierLoginResult) {
            is Success -> {
                GlobalLogger.log("Client logged in successfully with token: ${courierLoginResult.value}")
                val token = courierLoginResult.value.token
                val cookie =
                    ResponseCookie
                        .from("auth_token", token)
                        .path("/")
                        .maxAge(3600 * 12)
                        .build()
                ResponseEntity
                    .status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(
                        LoginOutputModel(
                            id = courierLoginResult.value.courierId,
                            username = courierLoginResult.value.username,
                            email = courierLoginResult.value.email,
                            bearer = courierLoginResult.value.token,
                        ),
                    )
            }
            is Failure -> {
                when (courierLoginResult.value) {
                    is CourierLoginError.BlankEmailOrPassword -> {
                        Problem.InvalidRequestContent.response(HttpStatus.BAD_REQUEST)
                    }
                    is CourierLoginError.CourierNotFound -> {
                        Problem.CourierNotFound.response(HttpStatus.NOT_FOUND)
                    }
                    is CourierLoginError.InvalidEmailOrPassword -> {
                        Problem.PasswordIsIncorrect.response(HttpStatus.UNAUTHORIZED)
                    }
                    is CourierLoginError.WrongPassword -> {
                        Problem.PasswordIsIncorrect.response(HttpStatus.UNAUTHORIZED)
                    }
                    else -> {
                        Problem.InternalServerError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                    }
                }
            }
        }
    }

    @DeleteMapping("/logout")
    fun logout(
        @RequestHeader("Authorization") token: String,
    ): ResponseEntity<Any> {
        val bearerToken = token.removePrefix("Bearer ").trim()
        val courierLogoutResult = courierService.logoutCourier(bearerToken)
        return when (courierLogoutResult) {
            is Either.Right -> {
                val expiredCookie =
                    ResponseCookie
                        .from("auth_token", "")
                        .path("/")
                        .maxAge(0)
                        .build()

                ResponseEntity
                    .status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                    .body(LogoutOutputModel(isLoggedOut = true))
            }
            is Either.Left -> {
                when (courierLogoutResult.value) {
                    is CourierLogoutError.SessionNotFound ->
                        Problem.SessionNotFound.response(HttpStatus.NOT_FOUND)
                    else ->
                        Problem.InternalServerError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
            }
        }
    }

    @PostMapping("/updateLocation")
    fun updateCourierLocation(
        @RequestBody input: LocationUpdateInputModel,
    ): ResponseEntity<Any> {
        val address =
            geocodingServices.reverseGeocode(
                input.newLocation.latitude,
                input.newLocation.longitude,
            )
        return when (address) {
            is Success -> {
                val updateLocationResult =
                    courierService.updateCourierLocation(
                        input.courierId,
                        input.newLocation,
                        address.value,
                    )

                when (updateLocationResult) {
                    is Success -> ResponseEntity.ok(updateLocationResult)
                    is Failure ->
                        when (updateLocationResult.value) {
                            is LocationUpdateError.CourierNotFound ->
                                Problem.CourierNotFound.response(HttpStatus.NOT_FOUND)
                            else ->
                                Problem.InternalServerError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                        }
                }
            }
            is Failure -> {
                when (address.value) {
                    is LocationUpdateError.InvalidCoordinates ->
                        Problem.InvalidCoordinates.response(HttpStatus.BAD_REQUEST)
                    else ->
                        Problem.InternalServerError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                }
            }
        }
    }

    /**
     * Sets the courier status to waiting for orders.
     */
    @PostMapping("/waitingOrders")
    fun startListening(input: StartListeningInputModel): ResponseEntity<Any> {
        val request =
            courierService.toggleAvailability(
                input.courierId,
            )

        return when (request) {
            is Success -> {
                // Handle successful order acceptance
                println("Courier is now available for orders")
                ResponseEntity.ok("Courier is now available for orders")
            }
            is Failure -> {
                when (request.value) {
                    is StateUpdateError.CourierNotFound -> {
                        Problem.CourierNotFound.response(HttpStatus.NOT_FOUND)
                    }
                    else -> {
                        Problem.InternalServerError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                    }
                }
            }
        }
    }

    /**
     * Cancels an ongoing order and updates the courier's status accordingly.
     */
    @PostMapping("/cancelOrder")
    fun cancelOrder() {
        TODO()
    }

    /**
     * Marks an order as picked up, indicating that the courier has collected it from the sender.
     */
    @PostMapping("/pickedUpOrder")
    fun pickUpOrder(
        @RequestBody input: PickupOrderInputModel,
    ): ResponseEntity<Any> {
        println("Received pickup request: $input")
        val request =
            courierService.pickupDelivery(
                requestId = input.requestId,
                courierId = input.courierId,
            )

        return when (request) {
            is Success -> {
                ResponseEntity.ok(true)
            }
            is Failure -> {
                when (request.value) {
                    is CourierDeliveryError.PackageAlreadyPickedUp -> {
                        Problem.PackageAlreadyPickedUp.response(HttpStatus.BAD_REQUEST)
                    }
                    is CourierDeliveryError.CourierNotNearPickup -> {
                        Problem.CourierNotNearPickup.response(HttpStatus.BAD_REQUEST)
                    }
                    else -> {
                        Problem.InternalServerError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                    }
                }
            }
        }
    }

    /**
     * Marks an order as delivered, indicating that the courier has successfully handed it to the recipient.
     */
    @PostMapping("/deliveredOrder")
    fun deliverOrder(
        @RequestBody input: DeliverOrderInputModel,
    ): ResponseEntity<Any> {
        val request =
            courierService.deliver(
                requestId = input.requestId,
                courierId = input.courierId,
            )

        return when (request) {
            is Success -> {
                // Handle successful order delivery
                println("Order delivered successfully")
                ResponseEntity.ok(true)
            }
            is Failure -> {
                when (request.value) {
                    is CourierDeliveryError.PackageAlreadyDelivered -> {
                        Problem.PackageAlreadyDelivered.response(HttpStatus.BAD_REQUEST)
                    }
                    else -> {
                        Problem.InternalServerError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                    }
                }
            }
        }
    }

    @GetMapping("/fetchDailyEarnings/{courierId}")
    fun fetchDailyEarnings(
        @PathVariable courierId: Int,
    ): ResponseEntity<Any> =
        when (val request = courierService.fetchDailyEarnings(courierId)) {
            is Success -> {
                ResponseEntity.ok(request.value)
            }
            is Failure -> {
                when (request.value) {
                    is CourierEarningsError.CourierNotFound -> {
                        Problem.CourierNotFound.response(HttpStatus.NOT_FOUND)
                    }
                    else -> {
                        Problem.InternalServerError.response(HttpStatus.INTERNAL_SERVER_ERROR)
                    }
                }
            }
        }

    /**
     * Completes an order, performing any necessary final operations (e.g., updating records or notifying the system).
     */
    @PostMapping("/completeOrder")
    fun completeOrder() {
        TODO()
    }
}
