package pt.isel

import com.example.utils.Either
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

        val client = usersService.getClientByToken(authorizationValue)

        return if (client is Either.Right) {
            AuthenticatedClient(
                client.value,
                authorizationValue,
            )
        } else {
            null
        }
    }

    fun processCourierAuthorizationHeaderValue(authorizationValue: String?): AuthenticatedCourier? {
        if (authorizationValue == null) return null

        val courier = usersService.getCourierByToken(authorizationValue)

        return if (courier is Either.Right) {
            AuthenticatedCourier(
                courier.value,
                authorizationValue,
            )
        } else {
            null
        }
    }

    companion object {
        const val SCHEME = "bearer"
    }
}
