package pt.isel.liftdrop.controller


import CourierService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/courier")
class CourierController(
    val courierService: CourierService,
) {

    @PostMapping("/register")
    fun registerClient() {
        TODO()
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
