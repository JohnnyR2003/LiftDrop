package liftdrop.repository

import pt.isel.liftdrop.Address
import pt.isel.pipeline.pt.isel.liftdrop.LocationDTO

interface LocationRepository {
    fun createLocation(
        location: LocationDTO,
        address: Address,
    ): Int

    fun getLocationById(id: Int): LocationDTO

    fun getRestaurantLocationByItem(item: String, restaurantName: String): LocationDTO // for restaurant location

    fun deleteDeliveryPath(deliveryId: Int): Boolean // for cancelled deliveries or deliveries that were marked as successfully delivered
}
