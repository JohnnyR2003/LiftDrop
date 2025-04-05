package pt.isel.liftdrop

import jakarta.inject.Named


@Named
class UserService {

    fun getUserByToken(token: String): User {
        println(token)
        TODO()
    }
}