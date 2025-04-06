package pt.isel.services

import com.example.CourierRepository
import com.example.UserRepository
import jakarta.inject.Named
import pt.isel.liftdrop.Courier

@Named("CourierService")
class CourierService(
    private val courierRepository: CourierRepository,
    private val userRepository: UserRepository,
) {
    fun registerCourier(courier: Courier): Courier {
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
        return courier
    }

    fun loginCourier(email: String, password: String): Courier? {
        val user = userRepository.findUserByEmail(email) ?: return null
        if (user.password != password) return null
        return courierRepository.getCourierByUserId(user.id.toInt())
    }

    fun deliver(packageId: String) {

    }
}
