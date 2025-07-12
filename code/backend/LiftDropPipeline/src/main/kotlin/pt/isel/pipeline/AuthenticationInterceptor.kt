package pt.isel.pipeline

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import pt.isel.liftdrop.AuthenticatedClient
import pt.isel.liftdrop.AuthenticatedCourier

@Component
class AuthenticationInterceptor(
    private val authorizationHeaderProcessor: RequestTokenProcessor,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (handler is HandlerMethod) {
            val authCookie = request.cookies?.find { it.name == "auth_token" }

            // Check for AuthorizedClient
            if (handler.methodParameters.any {
                    it.parameterType == AuthenticatedClient::class.java
                }
            ) {
                val client = authorizationHeaderProcessor
                    .processClientAuthorizationHeaderValue(authCookie?.value)

                if (client == null) {
                    response.status = 401
                    response.addHeader(NAME_WWW_AUTHENTICATE_HEADER, RequestTokenProcessor.SCHEME)
                    return false
                } else {
                    AuthenticatedClientArgumentResolver.addClientTo(client, request)
                }
            }

            // Check for AuthorizedCourier
            if (handler.methodParameters.any {
                    it.parameterType == AuthenticatedCourier::class.java
                }
            ) {
                val courier = authorizationHeaderProcessor
                    .processCourierAuthorizationHeaderValue(authCookie?.value)

                if (courier == null) {
                    response.status = 401
                    response.addHeader(NAME_WWW_AUTHENTICATE_HEADER, RequestTokenProcessor.SCHEME)
                    return false
                } else {
                    AuthenticatedCourierArgumentResolver.addCourierTo(courier, request)
                }
            }
        }

        return true
    }


    companion object {
        private const val NAME_WWW_AUTHENTICATE_HEADER = "WWW-Authenticate"
    }
}
