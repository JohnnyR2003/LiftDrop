package pt.isel.services

import jakarta.inject.Named
import liftdrop.repository.TransactionManager
import liftdrop.repository.UserRepository
import pt.isel.liftdrop.Client
import pt.isel.liftdrop.Courier
import pt.isel.liftdrop.User


@Named
class UserService(
    private val transactionManager: TransactionManager,
) {

    fun getCourierByToken(token: String): Courier? {
        return transactionManager.run {
            val userRepository = it.usersRepository
            return@run userRepository.findCourierByToken(token)
        }
    }

    fun getClientByToken(token: String): Client? {
        return transactionManager.run {
            val userRepository = it.usersRepository
            return@run userRepository.findClientByToken(token)
        }
    }
}
