package pt.isel.liftdrop

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(
    basePackages = [
        "pt.isel.services",
        "pt.isel.liftdrop",
        "liftdrop.repository.jdbi",
    ],
)
class LiftDropApplication

fun main(args: Array<String>) {
    runApplication<LiftDropApplication>(*args)
}
