package pt.isel.liftdrop.controller

import com.example.utils.Either
import com.example.utils.Failure
import com.example.utils.Success
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pt.isel.liftdrop.model.LoginInputModel
import pt.isel.liftdrop.model.LoginOutputModel
import pt.isel.liftdrop.model.RegisterCourierInputModel
import pt.isel.services.CourierService

@RestController
@RequestMapping("/courier")
class CourierController(
    val courierService: CourierService,
) {
    @PostMapping
    fun registerCourier(
        @RequestBody registerInput: RegisterCourierInputModel,
    ): ResponseEntity<Any> {
        val register =
            courierService
                .registerCourier(
                    email = registerInput.email,
                    password = registerInput.password,
                    name = registerInput.name,
                    location = registerInput.location,
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
                ) // NEEDS TO CHANGE IMPLEMENTATION BECAUSE IT LACKS SESSION MANAGEMENT

        return when (result) {
            is Either.Right -> {
                println("Client logged in successfully with token: ${result.value}")
                val token = result.value
                // Handle successful login
                val cookie =
                    ResponseCookie
                        .from("auth_token", token.toString())
                        .path("/") // Path for which the cookie is valid
                        .maxAge(3600 * 12) // Cookie expiration time
                        .build()
                ResponseEntity
                    .status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(LoginOutputModel(token.toString()))
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

    /**
     * Accepts an order and changes the courier's status accordingly.
     */
    @PostMapping("/acceptOrder")
    fun acceptOrder() {
        TODO()
    }

    /**
     * Declines an order, keeping the courier available for new requests.
     */
    @PostMapping("/declineOrder")
    fun declineOrder() {
        TODO()
    }

    /**
     * Sets the courier status to waiting for orders.
     */
    @PostMapping("/waitingOrders")
    fun waitingOrders() {
        TODO()
    }

    /**
     * Checks if the courier is currently waiting for orders.
     * @return true if the courier is available and waiting for new orders, false otherwise.
     */
    @GetMapping("/isWaitingOrders")
    fun isWaitingOrders() {
        TODO()
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
    fun pickedUpOrder() {
        TODO()
    }

    /**
     * Marks an order as delivered, indicating that the courier has successfully handed it to the recipient.
     */
    @PostMapping("/deliveredOrder")
    fun deliveredOrder() {
        TODO()
    }

    /**
     * Completes an order, performing any necessary final operations (e.g., updating records or notifying the system).
     */
    @PostMapping("/completeOrder")
    fun completeOrder() {
        TODO()
    }
}
