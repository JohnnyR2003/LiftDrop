package pt.isel.liftdrop_api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LiftDropApplication

fun main(args: Array<String>) {
    runApplication<LiftDropApplication>(*args)
}
