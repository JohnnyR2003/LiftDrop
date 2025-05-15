package pt.isel.liftdrop.controller

import com.example.utils.Either
import com.example.utils.Failure
import com.example.utils.Success
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.liftdrop.AuthenticatedCourier
import pt.isel.liftdrop.model.DeliverOrderInputModel
import pt.isel.liftdrop.model.LocationUpdateInputModel
import pt.isel.liftdrop.model.LoginInputModel
import pt.isel.liftdrop.model.LoginOutputModel
import pt.isel.liftdrop.model.PickupOrderInputModel
import pt.isel.liftdrop.model.RegisterCourierInputModel
import pt.isel.liftdrop.model.StartListeningInputModel
import pt.isel.services.CourierService
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
        val register =
            courierService
                .registerCourier(
                    email = registerInput.email,
                    password = registerInput.password,
                    name = registerInput.name,
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
        val result =
            courierService
                .loginCourier(
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
    fun logout(user: AuthenticatedCourier): ResponseEntity<Any> {
        val result = courierService.logoutCourier(user.token, user.courier.user.id)
        val expiredCookie =
            ResponseCookie
                .from("auth_token", "")
                .path("/")
                .maxAge(0) // Expire immediately
                .build()
        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
            .body("Logout successful")
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

        val result =
            courierService
                .updateCourierLocation(
                    input.courierId,
                    input.newLocation,
                    address,
                )

        return when (result) {
            is Success -> {
                // Handle successful location update
                println("Courier location updated successfully")
                ResponseEntity.ok("Location updated")
            }
            is Failure -> {
                // Handle location update error
                println("Failed to update location")
                ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update location")
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
                // Handle order acceptance error
                println("Failed to set courier to waiting for orders")
                ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to set courier to waiting for orders")
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
    fun pickUpOrder(input: PickupOrderInputModel): ResponseEntity<Any> {
        val request =
            courierService.pickupDelivery(
                requestId = input.requestId,
                courierId = input.courierId,
            )

        return when (request) {
            is Success -> {
                // Handle successful order pickup
                println("Order picked up successfully by courier with courierId: ${input.courierId}")
                ResponseEntity.ok("Order picked up")
            }
            is Failure -> {
                // Handle order pickup error
                println("Failed to pick up order")
                ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to pick up order")
            }
        }
    }

    /**
     * Marks an order as delivered, indicating that the courier has successfully handed it to the recipient.
     */
    @PostMapping("/deliveredOrder")
    fun deliverOrder(input: DeliverOrderInputModel): ResponseEntity<Any> {
        val request =
            courierService.deliver(
                requestId = 0,
                courierId = 0,
            )

        return when (request) {
            is Success -> {
                // Handle successful order delivery
                println("Order delivered successfully")
                ResponseEntity.ok("Order delivered")
            }
            is Failure -> {
                // Handle order delivery error
                println("Failed to deliver order")
                ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to deliver order")
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
