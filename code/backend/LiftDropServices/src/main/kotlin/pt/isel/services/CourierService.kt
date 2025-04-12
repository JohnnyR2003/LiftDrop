package pt.isel.services

import liftdrop.repository.TransactionManager
import org.springframework.stereotype.Service
import pt.isel.liftdrop.Location
import pt.isel.liftdrop.User
import pt.isel.liftdrop.UserRole

@Service
class CourierService(
    private val transactionManager: TransactionManager,
) {
    fun registerCourier(courier: User): Int {
        return transactionManager.run {
            val userRepository = it.usersRepository
            val courierRepository = it.courierRepository
            if (userRepository.findUserByEmail(courier.email) != null) {
                throw IllegalArgumentException("User with email ${courier.email} already exists")
            }
            if (courierRepository.getCourierByUserId(courier.id) != null) {
                throw IllegalArgumentException("Courier with id ${courier.id} already exists")
            }
            userRepository.createUser(
                email = courier.email,
                password = courier.password,
                name = courier.name,
                role = UserRole.COURIER,
            )
            val courierCreation =
                courierRepository.createCourier(
                    userId = courier.id,
                    currentLocation =
                        Location(
                            id = 0,
                            latitude = 0.0,
                            longitude = 0.0,
                            address = null,
                            name = "Current Location",
                        ),
                    isAvailable = false,
                )
            return@run courierCreation
        }
    }

    fun loginCourier(
        email: String,
        password: String,
    ): Int? =
        transactionManager.run {
            val courierRepository = it.courierRepository

            courierRepository.loginCourier(
                email = email,
                password = password,
            ) ?: throw IllegalArgumentException("Invalid email or password")
        }

    fun deliver(
        courierId: Int,
        packageId: Int,
    ) {
        transactionManager.run {
            val courierRepository = it.courierRepository
            val request = courierRepository.completeDelivery(packageId, courierId)
            if (!request) {
                throw IllegalStateException("Package already delivered")
            } else {
                println("Package $packageId delivered by courier $courierId")
            }
        }
    }

    fun acceptRequest(
        courierId: Int,
        requestId: Int,
    ) {
        val updated =
            transactionManager.run {
                val requestRepository = it.requestRepository
                requestRepository.updateRequest(
                    requestId,
                    courierId,
                    null,
                    null,
                )
            }
        if (!updated) {
            throw IllegalStateException("Request $requestId not accepted")
        } else {
            val accept =
                transactionManager.run {
                    val courierRepository = it.courierRepository
                    courierRepository.acceptRequest(
                        requestId,
                        courierId,
                    )
                }
            if (!accept) {
                throw IllegalStateException("Request $requestId not accepted")
            } else {
                println("Request $requestId accepted by courier $courierId")
                TODO()
            }
        }
    }
}
