package pt.isel.services

import com.example.utils.Either
import com.example.utils.failure
import com.example.utils.success
import liftdrop.repository.TransactionManager
import org.springframework.stereotype.Service
import pt.isel.liftdrop.Courier
import pt.isel.liftdrop.CourierWithLocation
import pt.isel.liftdrop.Location
import pt.isel.liftdrop.UserRole
import pt.isel.services.utils.Codify.codifyPassword
import pt.isel.services.utils.Codify.matchesPassword
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

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
    ): Either<CourierError, Boolean> {
        return transactionManager.run {
            val userRepository = it.usersRepository

            val user =
                userRepository.findUserByEmail(email) ?: return@run failure(CourierError.InvalidEmailOrPassword)

            if (user.role != UserRole.COURIER || matchesPassword(password, user.password)) {
                return@run failure(CourierError.InvalidEmailOrPassword)
            }

            success(true)
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


    fun sendToClosestCourier(
        requestId: Int,
        pickupLat: Double,
        pickupLon: Double,
        offset: Int = 0,
    ): Either<CourierError, CourierWithLocation> {
        return transactionManager.run {
            val courierRepository = it.courierRepository

            // 1. Get all available couriers and their locations
            val couriers = courierRepository.getAvailableCouriersWithLocation()
            if (couriers.isEmpty()) {
                return@run failure(CourierError.NoAvailableCouriers)
            }

            // 2. Sort couriers by distance
            val sortedCouriers = couriers.sortedBy { courier ->
                calculateDistance(pickupLat, pickupLon, courier.latitude, courier.longitude)
            }

            // 3. Pick a courier at the offset index
            if (offset >= sortedCouriers.size) {
                return@run failure(CourierError.NoCourierAvailable)
            }

            val selectedCourier = sortedCouriers[offset]
            return@run success(selectedCourier)
        }
    }




    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371e3 // radius of Earth in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
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
