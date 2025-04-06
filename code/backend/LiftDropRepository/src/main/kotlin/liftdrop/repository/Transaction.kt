package liftdrop.repository

interface Transaction {
    val usersRepository: UserRepository
    val clientRepository: ClientRepository
    val requestRepository: RequestRepository
    val courierRepository: CourierRepository
    val deliveryRepository: DeliveryRepository
    val locationRepository: LocationRepository

    fun rollback()
}
