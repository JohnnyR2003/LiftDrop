package pt.isel.liftdrop.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import pt.isel.liftdrop.AuthenticatedUser
import pt.isel.liftdrop.model.OrderInputModel

@RestController
class ClientController {
    @PostMapping("/client/makeOrder")
    fun makeOrder(
        user: AuthenticatedUser,
        @RequestBody order: OrderInputModel,
    ) {
        TODO()
    }

    @GetMapping("/client/getOrderStatus")
    fun getOrderStatus() {
        TODO()
    }

    @GetMapping("/client/getETA")
    fun getETA() {
        TODO()
    }
}
