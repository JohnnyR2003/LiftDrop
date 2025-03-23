package pt.isel.liftdrop.controller

class CourierController {

    /**
     * Accepts an order and changes the courier's status accordingly.
     */
    fun acceptOrder() {
        TODO()
    }

    /**
     * Declines an order, keeping the courier available for new requests.
     */
    fun declineOrder() {
        TODO()
    }

    /**
     * Sets the courier status to waiting for orders.
     */
    fun waitingOrders() {
        TODO()
    }

    /**
     * Checks if the courier is currently waiting for orders.
     * @return true if the courier is available and waiting for new orders, false otherwise.
     */
    fun isWaitingOrders() {
        TODO()
    }

    /**
     * Checks if the courier is currently delivering an order.
     * @return true if the courier is in the process of delivering an order, false otherwise.
     */
    fun isDeliveringOrders() {
        TODO()
    }

    /**
     * Cancels an ongoing order and updates the courier's status accordingly.
     */
    fun cancelOrder() {
        TODO()
    }

    /**
     * Marks an order as picked up, indicating that the courier has collected it from the sender.
     */
    fun pickedUpOrder() {
        TODO()
    }

    /**
     * Marks an order as delivered, indicating that the courier has successfully handed it to the recipient.
     */
    fun deliveredOrder() {
        TODO()
    }

    /**
     * Completes an order, performing any necessary final operations (e.g., updating records or notifying the system).
     */
    fun completeOrder() {
        TODO()
    }
}
