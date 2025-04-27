package pt.isel.liftdrop.controller

import com.example.utils.Failure
import com.example.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.services.UserService

@RestController
@RequestMapping("/api/user")
class UserController(
    val userService: UserService,
) {
    @PostMapping("/IdByToken")
    fun getCourierIdByToken(
        @RequestBody token: String,
    ): ResponseEntity<Any> =
        when (val result = userService.getCourierIdByToken(token)) {
            is Success -> {
                ResponseEntity.status(404).body("Courier not found")
            }
            is Failure -> {
                ResponseEntity.ok(result)
            }
        }
}
