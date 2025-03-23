package pt.isel.liftdrop.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CourierController {
    /**
     * Accepts an order and changes the courier's status accordingly.
     */
    @PostMapping("/courier/acceptOrder")
    fun acceptOrder() {
        TODO()
    }

    /**
     * Declines an order, keeping the courier available for new requests.
     */
    @PostMapping("/courier/declineOrder")
    fun declineOrder() {
        TODO()
    }

    /**
     * Sets the courier status to waiting for orders.
     */
    @PostMapping("/courier/waitingOrders")
    fun waitingOrders() {
        TODO()
    }

    /**
     * Checks if the courier is currently waiting for orders.
     * @return true if the courier is available and waiting for new orders, false otherwise.
     */
    @GetMapping("/courier/isWaitingOrders")
    fun isWaitingOrders() {
        TODO()
    }

    /**
     * Cancels an ongoing order and updates the courier's status accordingly.
     */
    @PostMapping("/courier/cancelOrder")
    fun cancelOrder() {
        TODO()
    }

    /**
     * Marks an order as picked up, indicating that the courier has collected it from the sender.
     */
    @PostMapping("/courier/pickedUpOrder")
    fun pickedUpOrder() {
        TODO()
    }

    /**
     * Marks an order as delivered, indicating that the courier has successfully handed it to the recipient.
     */
    @PostMapping("/courier/deliveredOrder")
    fun deliveredOrder() {
        TODO()
    }

    /**
     * Completes an order, performing any necessary final operations (e.g., updating records or notifying the system).
     */
    @PostMapping("/courier/completeOrder")
    fun completeOrder() {
        TODO()
    }
}
