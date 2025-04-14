package pt.isel.services

import jakarta.inject.Named
import pt.isel.liftdrop.Client
import pt.isel.liftdrop.Courier
import pt.isel.liftdrop.User


@Named
class UserService {

    fun getUserByToken(token: String): User {
        println(token)
        TODO()
    }

    fun getCourierByToken(token: String): Courier {
        println(token)
        TODO()
    }

    fun getClientByToken(token: String): Client {
        println(token)
        TODO()
    }
}