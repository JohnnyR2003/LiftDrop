package pt.isel.liftdrop.controller

import com.example.utils.Failure
import com.example.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pt.isel.liftdrop.model.GetCourierIdOutputModel
import pt.isel.services.user.UserService

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
            is Success -> ResponseEntity.ok(GetCourierIdOutputModel(result.value.toString()))
            is Failure -> ResponseEntity.status(404).body("Courier not found")
        }
    }
}
