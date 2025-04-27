package pt.isel.services

import com.example.utils.Either
import com.example.utils.failure
import com.example.utils.success
import liftdrop.repository.TransactionManager
import org.springframework.stereotype.Service
import pt.isel.liftdrop.Courier
import pt.isel.liftdrop.CourierWithLocation
import pt.isel.liftdrop.UserRole
import pt.isel.pipeline.pt.isel.liftdrop.LocationDTO
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
                    isAvailable = false,
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
                courierRepository.loginCourier(
                    email = email,
                    password = password,
                ) ?: return@run failure(CourierError.InvalidEmailOrPassword)

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
        newLocation: LocationDTO,
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

    fun fetchClosestCouriers(
        pickupLat: Double,
        pickupLon: Double,
        offset: Int = 0,
    ): Either<CourierError, CourierWithLocation> {
        return transactionManager.run {
            val courierRepository = it.courierRepository

            // 1. Get all available couriers and their locations(already sorted by distance)
            val couriers = courierRepository.getClosestCouriersAvailable(pickupLat, pickupLon)
            if (couriers.isEmpty()) {
                return@run failure(CourierError.NoAvailableCouriers)
            }

            // 2. Pick a courier at the offset index
            if (offset >= couriers.size) {
                return@run failure(CourierError.NoCourierAvailable)
            }

            val selectedCourier = couriers[offset]
            return@run success(selectedCourier)
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
}
