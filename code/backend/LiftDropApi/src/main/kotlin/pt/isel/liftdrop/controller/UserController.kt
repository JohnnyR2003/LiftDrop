package pt.isel.liftdrop.controller

import com.example.utils.Failure
import com.example.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pt.isel.services.UserService

@RestController
@RequestMapping("/user")
class UserController(
    val userService: UserService,
) {
    @PostMapping("/IdByToken")
    fun getCourierIdByToken(
        @RequestHeader("Authorization") authHeader: String,
    ): ResponseEntity<Any> {
        val token = authHeader.removePrefix("Bearer ").trim()

        return when (val result = userService.getCourierIdByToken(token)) {
            is Success -> ResponseEntity.ok(result)
            is Failure -> ResponseEntity.status(404).body("Courier not found")
        }
    }
}
