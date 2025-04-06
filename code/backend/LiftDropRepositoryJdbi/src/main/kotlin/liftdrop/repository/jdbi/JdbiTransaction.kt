package liftdrop.repository.jdbi

import liftdrop.repository.Transaction
import org.jdbi.v3.core.Handle

class JdbiTransaction(
    private val handle: Handle,
) : Transaction {
    override val usersRepository = JdbiUserRepository(handle)
    override val clientRepository = JdbiClientRepository(handle)
    override val requestRepository = JdbiRequestRepository(handle)
    override val courierRepository = JdbiCourierRepository(handle)
    override val deliveryRepository = JdbiDeliveryRepository(handle)
    override val locationRepository = JdbiLocationRepository(handle)

    override fun rollback() {
        handle.rollback()
    }
}
