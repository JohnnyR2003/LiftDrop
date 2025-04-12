package pt.isel.liftdrop

import liftdrop.repository.jdbi.configureWithAppRequirements
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@SpringBootApplication
@ComponentScan(
    basePackages = [
        "pt.isel.services",
        "pt.isel.liftdrop",
    ],
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
}

fun main(args: Array<String>) {
    runApplication<LiftDropApplication>(*args)
}
