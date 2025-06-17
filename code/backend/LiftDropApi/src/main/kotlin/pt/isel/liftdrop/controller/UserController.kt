package pt.isel.liftdrop.controller

import com.example.utils.Failure
import com.example.utils.Success
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pt.isel.liftdrop.Uris
import pt.isel.liftdrop.model.GetCourierIdOutputModel
import pt.isel.services.user.UserService

@RestController
@RequestMapping(Uris.User.BASE)
class UserController(
    val userService: UserService,
) {
    @PostMapping(Uris.User.ID_BY_TOKEN)
    fun getCourierIdByToken(
        @RequestHeader("Authorization") authHeader: String,
    ): ResponseEntity<Any> {
        val token = authHeader.removePrefix("Bearer ").trim()

        return when (val result = userService.getCourierIdByToken(token)) {
            is Success -> ResponseEntity.ok(GetCourierIdOutputModel(result.value.toString()))
            is Failure -> ResponseEntity.status(404).body("You are not logged in, please log in first.")
        }
    }
}
