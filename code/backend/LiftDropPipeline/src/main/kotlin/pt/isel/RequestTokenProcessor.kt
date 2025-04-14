package pt.isel

import org.springframework.stereotype.Component
import pt.isel.liftdrop.AuthenticatedClient
import pt.isel.liftdrop.AuthenticatedCourier
import pt.isel.services.UserService

@Component
class RequestTokenProcessor(
    private val usersService: UserService,
) {
    fun processClientAuthorizationHeaderValue(authorizationValue: String?): AuthenticatedClient? {
        if (authorizationValue == null) return null

        return usersService.getClientByToken(authorizationValue)?.let {
            AuthenticatedClient(
                it,
                authorizationValue,
            )
        }
    }

    fun processCourierAuthorizationHeaderValue(authorizationValue: String?): AuthenticatedCourier? {
        if (authorizationValue == null) return null

        return usersService.getCourierByToken(authorizationValue)?.let {
            AuthenticatedCourier(
                it,
                authorizationValue,
            )
        }
    }

    companion object {
        const val SCHEME = "bearer"
    }
}
