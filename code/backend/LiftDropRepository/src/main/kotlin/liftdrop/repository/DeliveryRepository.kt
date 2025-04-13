package liftdrop.repository

import pt.isel.liftdrop.RequestStatus

interface DeliveryRepository {
    fun updateDeliveryStatus(
        deliveryId: Int,
        status: RequestStatus,
    ): Boolean

    fun deleteDelivery(courierId: Int, deliveryId: Int): Boolean // for cancelled deliveries
}
