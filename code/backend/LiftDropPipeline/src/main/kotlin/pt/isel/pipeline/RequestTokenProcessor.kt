package pt.isel.im_pipeline.pt.isel.pipeline

import pt.isel.liftdrop_services.UserService
import org.springframework.stereotype.Component
import pt.isel.liftdrop_domain.AuthenticatedUser

@Component
class RequestTokenProcessor(
    private val usersService: UserService,
) {
    fun processAuthorizationHeaderValue(authorizationValue: String?): AuthenticatedUser? {
        if (authorizationValue == null) {
            return null
        }
        return AuthenticatedUser(
            usersService.getUserByToken(authorizationValue),
            authorizationValue,
        )
    }

    companion object {
        const val SCHEME = "bearer"
    }
}
