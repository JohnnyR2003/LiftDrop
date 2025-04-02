package pt.isel.im_pipeline.pt.isel.pipeline

import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import pt.isel.liftdrop.AuthenticatedUser


@Component
class AuthenticatedUserArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) = parameter.parameterType == AuthenticatedUser::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any? {
        val request =
            webRequest.getNativeRequest(HttpServletRequest::class.java)
                ?: throw IllegalStateException("Does not have a HttpServletRequest")

        return getUserFrom(request) ?: throw IllegalStateException("No AuthenticatedUser in request")
    }

    companion object {
        private const val KEY = "AuthenticatedUserArgumentResolver"

        fun addUserTo(
            user: AuthenticatedUser,
            request: HttpServletRequest,
        ) = request.setAttribute(KEY, user)

        fun getUserFrom(request: HttpServletRequest): AuthenticatedUser? =
            request.getAttribute(KEY)?.let {
                it as? AuthenticatedUser
            }


    }
}
