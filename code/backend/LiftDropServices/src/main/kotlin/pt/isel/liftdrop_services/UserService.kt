package pt.isel.liftdrop_services

import jakarta.inject.Named
import pt.isel.liftdrop_domain.User


@Named
class UserService {

    fun getUserByToken(token: String): User {
        println(token)
        TODO()
    }
}