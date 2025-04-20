package pt.isel

import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import pt.isel.liftdrop.AuthenticatedClient
import pt.isel.liftdrop.AuthenticatedCourier

@Component
class AuthenticatedCourierArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) = parameter.parameterType == AuthenticatedCourier::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any {
        val request =
            webRequest.getNativeRequest(HttpServletRequest::class.java)
                ?: throw IllegalStateException("Does not have a HttpServletRequest")

        return getCourierFrom(request) ?: throw IllegalStateException("No AuthenticatedCourier in request")
    }

    companion object {
        private const val KEY = "AuthenticatedCourierArgumentResolver"

        fun addCourierTo(
            courier: AuthenticatedCourier,
            request: HttpServletRequest,
        ) = request.setAttribute(KEY, courier)

        fun getCourierFrom(request: HttpServletRequest): AuthenticatedCourier? =
            request.getAttribute(KEY)?.let {
                it as? AuthenticatedCourier
            }
    }
}

@Component
class AuthenticatedClientArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) = parameter.parameterType == AuthenticatedClient::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any {
        val request =
            webRequest.getNativeRequest(HttpServletRequest::class.java)
                ?: throw IllegalStateException("Does not have a HttpServletRequest")

        return getClientFrom(request) ?: throw IllegalStateException("No AuthenticatedClient in request")
    }

    companion object {
        private const val KEY = "AuthenticatedClientArgumentResolver"

        fun addClientTo(
            client: AuthenticatedClient,
            request: HttpServletRequest,
        ) = request.setAttribute(KEY, client)

        fun getClientFrom(request: HttpServletRequest): AuthenticatedClient? =
            request.getAttribute(KEY)?.let {
                it as? AuthenticatedClient
            }
    }
}
