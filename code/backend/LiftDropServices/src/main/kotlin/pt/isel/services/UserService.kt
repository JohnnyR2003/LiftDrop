package pt.isel.services

import com.example.utils.Either
import com.example.utils.failure
import com.example.utils.success
import jakarta.inject.Named
import liftdrop.repository.TransactionManager
import pt.isel.liftdrop.Client
import pt.isel.liftdrop.Courier
import java.lang.IllegalArgumentException

@Named
class UserService(
    private val transactionManager: TransactionManager,
) {
    fun getCourierIdByToken(token: String): Either<IllegalArgumentException, Int> {
        return transactionManager.run {
            val userRepository = it.usersRepository
            val courierId = userRepository.getCourierIdByToken(token)
            if (courierId == null) {
                return@run failure(IllegalArgumentException("Courier not found"))
            } else {
                success(courierId)
            }
        }
    }

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
