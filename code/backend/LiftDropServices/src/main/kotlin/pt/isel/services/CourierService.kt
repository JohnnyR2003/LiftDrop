package pt.isel.services

import com.example.utils.Either
import com.example.utils.failure
import com.example.utils.success
import liftdrop.repository.TransactionManager
import org.springframework.stereotype.Service
import pt.isel.liftdrop.Courier
import pt.isel.liftdrop.Location
import pt.isel.liftdrop.UserRole
import pt.isel.services.utils.Codify
import pt.isel.services.utils.Codify.codifyPassword

sealed class CourierError {
    data object CourierNotFound : CourierError()

    data object UserNotFound : CourierError()

    data object InvalidEmailOrPassword : CourierError()

    data object PackageAlreadyDelivered : CourierError()

    data object RequestNotAccepted : CourierError()

    data object CourierEmailAlreadyExists : CourierError()
}

@Service
class CourierService(
    private val transactionManager: TransactionManager,
) {
    fun registerCourier(
        email: String,
        password: String,
        name: String,
        location: Location,
    ): Either<CourierError, Int> =
        transactionManager.run {
            val userRepository = it.usersRepository
            val courierRepository = it.courierRepository
            if (userRepository.findUserByEmail(email) != null) {
                return@run failure(CourierError.CourierEmailAlreadyExists)
            }
            val id =
                userRepository.createUser(
                    email = email,
                    password = password.codifyPassword(),
                    name = name,
                    role = UserRole.COURIER,
                )
            val courierCreation =
                courierRepository.createCourier(
                    userId = id,
                    currentLocation = location,
                    isAvailable = false,
                )
            success(courierCreation)
        }

    fun loginCourier(
        email: String,
        password: String,
    ): Either<CourierError, Int> {
        return transactionManager.run {
            val courierRepository = it.courierRepository

            val c =
                courierRepository.loginCourier(
                    email = email,
                    password = password.codifyPassword(),
                ) ?: return@run failure(CourierError.InvalidEmailOrPassword)

            success(c)
        }
    }

    fun acceptRequest(
        courierId: Int,
        requestId: Int,
    ): Either<CourierError, Boolean> {
        return transactionManager.run {
            val courierRepository = it.courierRepository
            val request = courierRepository.acceptRequest(requestId, courierId)
            if (!request) {
                return@run failure(CourierError.RequestNotAccepted)
            } else {
                success(true)
            }
        }
    }

    fun deliver(
        courierId: Int,
        packageId: Int,
    ): Either<CourierError, Boolean> {
        return transactionManager.run {
            val courierRepository = it.courierRepository
            val request = courierRepository.completeDelivery(packageId, courierId)
            if (!request) {
                return@run failure(CourierError.PackageAlreadyDelivered)
            } else {
                success(true)
            }
        }
    }

    fun getCourierById(courierId: Int): Either<CourierError, Courier> {
        return transactionManager.run {
            val courierRepository = it.courierRepository
            val courier = courierRepository.getCourierByUserId(courierId)
            if (courier == null) {
                return@run failure(CourierError.CourierNotFound)
            } else {
                success(courier)
            }
        }
    }

    fun updateCourierLocation(
        courierId: Int,
        newLocation: Location,
    ): Either<CourierError, Boolean> {
        return transactionManager.run {
            val courierRepository = it.courierRepository
            val updated = courierRepository.updateCourierLocation(courierId, newLocation)
            if (!updated) {
                return@run failure(CourierError.CourierNotFound)
            } else {
                success(true)
            }
        }
    }
}
