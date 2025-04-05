package com.example

interface DeliveryRepository {
    fun createDelivery(
        requestId: Int,
        startTime: String,
        endTime: String,
        deliveryStatus: String,
    ): Int

    fun completeDelivery(deliveryId: Int): Boolean

    fun deleteDelivery(deliveryId: Int): Boolean // for cancelled deliveries
}
