package pt.isel.liftdrop

import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import pt.isel.pipeline.AuthenticatedClientArgumentResolver
import pt.isel.pipeline.AuthenticatedCourierArgumentResolver
import pt.isel.pipeline.AuthenticationInterceptor

@Configuration
class PipelineConfig(
    val authenticationInterceptor: AuthenticationInterceptor,
    val authenticatedCourierArgumentResolver: AuthenticatedCourierArgumentResolver,
    val authenticatedClientArgumentResolver: AuthenticatedClientArgumentResolver,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authenticationInterceptor)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authenticatedCourierArgumentResolver)
        resolvers.add(authenticatedClientArgumentResolver)
    }
}
