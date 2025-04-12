package liftdrop.repository

import pt.isel.liftdrop.Status

interface DeliveryRepository {
    fun createDelivery(
        requestId: Int,
        startTime: String,
        endTime: String,
    ): Int

    fun updateDeliveryStatus(
        deliveryId: Int,
        status: Status,
    ): Boolean

    fun completeDelivery(deliveryId: Int): Boolean

    fun deleteDelivery(deliveryId: Int): Boolean // for cancelled deliveries
}
