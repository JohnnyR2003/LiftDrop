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
import pt.isel.liftdrop.model.DeliverOrderInputModel
import pt.isel.liftdrop.model.LocationUpdateInputModel
import pt.isel.liftdrop.model.LoginInputModel
import pt.isel.liftdrop.model.LoginOutputModel
import pt.isel.liftdrop.model.LogoutOutputModel
import pt.isel.liftdrop.model.PickupOrderInputModel
import pt.isel.liftdrop.model.RegisterCourierInputModel
import pt.isel.liftdrop.model.RegisterCourierOutputModel
import pt.isel.liftdrop.model.StartListeningInputModel
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
                        println("Courier email already exists")
                        ResponseEntity
                            .status(HttpStatus.CONFLICT)
                            .body("Courier email already exists")
                    }

                    else -> {
                        ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to register courier")
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
                val token = courierLoginResult.value
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
            is Failure -> {
                when (courierLoginResult.value) {
                    is CourierLoginError.BlankEmailOrPassword -> {
                        ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body("Email and password cannot be blank")
                    }
                    is CourierLoginError.CourierNotFound -> {
                        ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body("Courier not found")
                    }
                    is CourierLoginError.InvalidEmailOrPassword -> {
                        ResponseEntity
                            .status(HttpStatus.UNAUTHORIZED)
                            .body("Invalid email or password")
                    }
                    is CourierLoginError.WrongPassword -> {
                        ResponseEntity
                            .status(HttpStatus.UNAUTHORIZED)
                            .body("Wrong password")
                    }
                    else -> {
                        ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to log in courier")
                    }
                }
            }
        }
    }

    @DeleteMapping("/logout")
    fun logout(
        @RequestHeader("Authorization") token: String,
        @RequestBody user: LogoutOutputModel,
    ): ResponseEntity<Any> {
        val bearerToken = token.removePrefix("Bearer ").trim()
        val courierLogoutResult = courierService.logoutCourier(bearerToken, user.courierId)
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
                    .body("Logout successful")
            }
            is Either.Left -> {
                println("Failed to log out courier")
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
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
                                ResponseEntity
                                    .status(HttpStatus.NOT_FOUND)
                                    .body("Courier not found")
                            else ->
                                ResponseEntity
                                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body("Failed to update courier location")
                        }
                }
            }
            is Failure -> {
                when (address.value) {
                    is LocationUpdateError.InvalidCoordinates ->
                        ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body("Invalid coordinates provided")
                    else ->
                        ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to reverse geocode location")
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
                        ResponseEntity
                            .status(HttpStatus.NOT_FOUND)
                            .body("Courier not found")
                    }
                    else -> {
                        ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to set courier status to waiting for orders")
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
                        ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body("Package has already been picked up")
                    }
                    else -> {
                        ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to pick up order")
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
                        ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body("Package has already been delivered")
                    }
                    else -> {
                        ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to deliver order")
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
                ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching courier's daily earnings")
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
