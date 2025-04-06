package pt.isel.services

import jakarta.inject.Named
import pt.isel.liftdrop.User


@Named
class UserService {

    fun getUserByToken(token: String): User {
        println(token)
        TODO()
    }
}