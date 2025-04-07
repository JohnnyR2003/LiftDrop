package pt.isel.services


import jakarta.inject.Named
import liftdrop.repository.TransactionManager
import pt.isel.liftdrop.Courier

@Named("CourierService")
class CourierService(
    private val transactionManager: TransactionManager
) {
    fun registerCourier(courier: Courier): Courier {
        transactionManager.run {
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
            )
            courierRepository.createCourier(
                userId = courier.id.toInt(),
                currentLocation = courier.currentLocation,
                isAvailable = courier.isAvailable,
            )
        }
        return courier
    }

    fun loginCourier(email: String, password: String): Courier? {
        return transactionManager.run {
            val userRepository = it.usersRepository
            val courierRepository = it.courierRepository
            val user = userRepository.findUserByEmail(email)
            if (user != null && user.password == password) {
                courierRepository.getCourierByUserId(user.id)
            } else {
                null
            }
        }
    }

    fun deliver(packageId: String) {

    }

    fun acceptRequest(
        courierId: Long,
        requestId: Long,
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
