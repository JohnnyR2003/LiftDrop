package pt.isel.services.courier

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

@Service
class CourierService(
    private val transactionManager: TransactionManager,
) {
    fun registerCourier(
        email: String,
        password: String,
        name: String,
    ): Either<CourierCreationError, Int> =
        transactionManager.run {
            val userRepository = it.usersRepository
            val courierRepository = it.courierRepository

            if (userRepository.findUserByEmail(email) != null) return@run failure(CourierCreationError.CourierEmailAlreadyExists)

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
    ): Either<CourierLoginError, UserDetails> =
        transactionManager.run {
            val courierRepository = it.courierRepository
            val userRepository = it.usersRepository

            if (email.isBlank() || password.isBlank()) return@run failure(CourierLoginError.BlankEmailOrPassword)

            val user =
                userRepository.findUserByEmail(email)
                    ?: return@run failure(CourierLoginError.CourierNotFound)

            val passwordFromDatabase =
                courierRepository
                    .loginCourier(
                        email = email,
                        password = password,
                    )?.second ?: return@run failure(CourierLoginError.InvalidEmailOrPassword)

            val sessionToken = UUID.randomUUID().toString()

            courierRepository.createCourierSession(
                user.id,
                sessionToken,
            )

            return@run when (matchesPassword(password, passwordFromDatabase)) {
                true ->
                    success(
                        UserDetails(
                            courierId = user.id,
                            username = user.name,
                            email = user.email,
                            token = sessionToken,
                        ),
                    )
                false -> failure(CourierLoginError.WrongPassword)
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
    ): Either<CourierDeliveryError, Boolean> {
        return transactionManager.run {
            val courierRepository = it.courierRepository
            val locationRepository = it.locationRepository

            val isCourierNearPickup = locationRepository.isCourierNearPickup(courierId, requestId)
            if (!isCourierNearPickup) {
                return@run failure(CourierDeliveryError.CourierNotNearPickup)
            }
            val request = courierRepository.pickupDelivery(requestId, courierId)
            if (!request) {
                return@run failure(CourierDeliveryError.PackageAlreadyPickedUp)
            } else {
                success(true)
            }
        }
    }

    fun deliver(
        requestId: Int,
        courierId: Int,
    ): Either<CourierDeliveryError, Boolean> {
        return transactionManager.run {
            val courierRepository = it.courierRepository
            val locationRepository = it.locationRepository

            val isCourierNearDropOff = locationRepository.isCourierNearDropOff(courierId, requestId)
            if (!isCourierNearDropOff) {
                return@run failure(CourierDeliveryError.CourierNotNearDropOff)
            }
            val request = courierRepository.completeDelivery(requestId, courierId)
            if (!request) {
                return@run failure(CourierDeliveryError.PackageAlreadyDelivered)
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
    ): Either<LocationUpdateError, Boolean> {
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
                return@run failure(LocationUpdateError.CourierNotFound)
            } else {
                success(true)
            }
        }
    }

    fun fetchDailyEarnings(courierId: Int): Either<CourierEarningsError, Double> {
        return transactionManager.run {
            val courierRepository = it.courierRepository
            val dailyEarnings = courierRepository.fetchDailyEarnings(courierId)
            if (dailyEarnings != null) {
                success(dailyEarnings)
            } else {
                return@run failure(CourierEarningsError.CourierNotFound)
            }
        }
    }

    fun toggleAvailability(courierId: Int): Either<StateUpdateError, Boolean> {
        return transactionManager.run {
            val courierRepository = it.courierRepository
            val updated = courierRepository.toggleAvailability(courierId)
            if (!updated) {
                return@run failure(StateUpdateError.CourierNotFound)
            } else {
                success(true)
            }
        }
    }

    fun logoutCourier(token: String): Either<CourierLogoutError, Boolean> {
        return transactionManager.run {
            val courierRepository = it.courierRepository
            val result = courierRepository.logoutCourier(token)
            if (result) {
                return@run success(true)
            } else {
                return@run failure(CourierLogoutError.SessionNotFound)
            }
        }
    }
}
