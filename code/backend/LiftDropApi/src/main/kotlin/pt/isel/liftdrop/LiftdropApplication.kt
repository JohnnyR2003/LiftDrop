package pt.isel.liftdrop

import liftdrop.repository.jdbi.configureWithAppRequirements
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import pt.isel.services.CourierWebSocketHandler

@SpringBootApplication(
    scanBasePackages = ["pt.isel.liftdrop", "liftdrop.repository.jdbi", "pt.isel.services"],
    exclude = [org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration::class],
)
class LiftDropApplication {
    @Bean
    fun jdbi() =
        Jdbi
            .create(
                PGSimpleDataSource().apply {
                    setUrl(EnvironmentApp.getDbUrl())
                    user = EnvironmentApp.getDbUser()
                    password = EnvironmentApp.getDbPassword()
                },
            ).configureWithAppRequirements()

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Configuration
    @EnableWebSocket
    class CourierWebSocketConfig(
        private val courierWebSocketHandler: CourierWebSocketHandler,
    ) : WebSocketConfigurer {
        override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
            registry
                .addHandler(courierWebSocketHandler, "/ws/courier")
                .setAllowedOrigins("*") // Adjust if needed
        }
    }
}

fun main(args: Array<String>) {
    runApplication<LiftDropApplication>(*args)
}
