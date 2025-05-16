package pt.isel.services

import com.example.utils.Either
import com.example.utils.failure
import com.example.utils.success
import liftdrop.repository.TransactionManager
import org.springframework.stereotype.Service
import pt.isel.liftdrop.Address
import pt.isel.liftdrop.Courier
import pt.isel.liftdrop.LocationDTO
import pt.isel.liftdrop.UserRole
import pt.isel.services.utils.Codify.encodePassword
import pt.isel.services.utils.Codify.matchesPassword
import java.util.*

sealed class CourierError {
    data object CourierNotFound : CourierError()

    data object UserNotFound : CourierError()

    data object InvalidEmailOrPassword : CourierError()

    data object PackageAlreadyDelivered : CourierError()

    data object RequestNotAccepted : CourierError()

    data object CourierEmailAlreadyExists : CourierError()

    data object NoAvailableCouriers : CourierError()

    data object NoCourierAvailable : CourierError()
}

@Service
class CourierService(
    private val transactionManager: TransactionManager,
) {
    fun registerCourier(
        email: String,
        password: String,
        name: String,
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
                    password = password.encodePassword(),
                    name = name,
                    role = UserRole.COURIER,
                )
            val courierCreation =
                courierRepository.createCourier(
                    userId = id,
                    isAvailable = true,
                )
            success(courierCreation)
        }

    fun loginCourier(
        email: String,
        password: String,
    ): Either<CourierError, String> =
        transactionManager.run {
            val courierRepository = it.courierRepository
            val userRepository = it.usersRepository

            val passwordFromDatabase =
                courierRepository
                    .loginCourier(
                        email = email,
                        password = password,
                    )?.second ?: return@run failure(CourierError.InvalidEmailOrPassword)

            val user =
                userRepository.findUserByEmail(email)
                    ?: return@run failure(CourierError.UserNotFound)

            if (user.role != UserRole.COURIER) {
                return@run failure(CourierError.InvalidEmailOrPassword)
            }

            val sessionToken = UUID.randomUUID().toString()

            courierRepository.createCourierSession(
                user.id,
                sessionToken,
            )

            return@run when (matchesPassword(password, passwordFromDatabase)) {
                true -> success(sessionToken)
                false -> failure(CourierError.InvalidEmailOrPassword)
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

    fun declineRequest(
        courierId: Int,
        requestId: Int,
    ): Either<CourierError, Boolean> {
        return transactionManager.run {
            val courierRepository = it.courierRepository
            val request = courierRepository.declineRequest(courierId, requestId)
            if (!request) {
                return@run failure(CourierError.RequestNotAccepted)
            } else {
                success(true)
            }
        }
    }

    fun pickupDelivery(
        requestId: Int,
        courierId: Int,
    ): Either<CourierError, Boolean> {
        return transactionManager.run {
            val courierRepository = it.courierRepository
            val request = courierRepository.pickupDelivery(requestId, courierId)
            if (!request) {
                return@run failure(CourierError.PackageAlreadyDelivered)
            } else {
                success(true)
            }
        }
    }

    fun deliver(
        requestId: Int,
        courierId: Int,
    ): Either<CourierError, Boolean> {
        return transactionManager.run {
            val courierRepository = it.courierRepository
            val request = courierRepository.completeDelivery(requestId, courierId)
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
        newLocation: LocationDTO,
        address: Address,
    ): Either<CourierError, Boolean> {
        return transactionManager.run {
            val courierRepository = it.courierRepository
            val locationRepository = it.locationRepository
            val locationId =
                locationRepository.createLocation(
                    newLocation,
                    address,
                )
            val updated = courierRepository.updateCourierLocation(courierId, locationId)
            if (!updated) {
                return@run failure(CourierError.CourierNotFound)
            } else {
                success(true)
            }
        }
    }

    fun toggleAvailability(courierId: Int): Either<CourierError, Boolean> {
        return transactionManager.run {
            val courierRepository = it.courierRepository
            val updated = courierRepository.toggleAvailability(courierId)
            if (!updated) {
                return@run failure(CourierError.CourierNotFound)
            } else {
                success(true)
            }
        }
    }

    fun logoutCourier(
        token: String,
        courierId: Int,
    ): Either<CourierError, Boolean> {
        return transactionManager.run {
            val clientRepository = it.courierRepository
            val result = clientRepository.logoutCourier(token, courierId)
            if (result) {
                return@run success(true)
            } else {
                return@run failure(CourierError.CourierNotFound)
            }
        }
    }
}
