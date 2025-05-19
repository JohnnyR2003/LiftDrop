package pt.isel.services

import com.example.utils.Either
import com.example.utils.failure
import com.example.utils.success
import jakarta.inject.Named
import liftdrop.repository.TransactionManager
import pt.isel.liftdrop.Client
import pt.isel.liftdrop.Courier

sealed class UserError {
    data object ClientNotFound : UserError()

    data object CourierNotFound : UserError()
}

@Named
class UserService(
    private val transactionManager: TransactionManager,
) {
    fun getCourierIdByToken(token: String): Either<UserError, Int> {
        return transactionManager.run {
            println("the following token was received in the backend for fetching purposes: $token")
            val userRepository = it.usersRepository
            val courierId = userRepository.getCourierIdByToken(token)
            println("the following courierId was fetched from the database: $courierId")
            if (courierId == null) {
                return@run failure(UserError.CourierNotFound)
            } else {
                success(courierId)
            }
        }
    }

    fun getCourierByToken(token: String): Either<UserError, Courier>? {
        return transactionManager.run {
            val userRepository = it.usersRepository
            val courier = userRepository.findCourierByToken(token)
            if (courier == null) {
                return@run failure(UserError.CourierNotFound)
            } else {
                success(courier)
            }
        }
    }

    fun getClientByToken(token: String): Either<UserError, Client>? {
        println("I reached the getClientByToken")
        println("token: $token")
        return transactionManager.run {
            val userRepository = it.usersRepository
            val client = userRepository.findClientByToken(token)
            if (client == null) {
                return@run failure(UserError.ClientNotFound)
            } else {
                success(client)
            }
        }
    }
}
