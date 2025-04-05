package com.example

import pt.isel.liftdrop.Location

interface LocationRepository {
    fun createLocation(
        courierId: Int,
        deliveryId: Int,
        location: Location,
    ): Int

    fun deleteDeliveryPath(deliveryId: Int): Boolean // for cancelled deliveries or deliveries that were marked as successfully delivered
}
