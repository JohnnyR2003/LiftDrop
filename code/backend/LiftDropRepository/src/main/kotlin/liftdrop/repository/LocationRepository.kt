package liftdrop.repository

import pt.isel.liftdrop.Address
import pt.isel.liftdrop.LocationDTO

interface LocationRepository {
    fun createLocation(
        location: LocationDTO,
        address: Address,
    ): Int

    fun getLocationById(id: Int): LocationDTO

    fun getClosestRestaurantLocation(
        restaurantName: String,
        clientLocationId: Int,
    ): Pair<Int, LocationDTO>?

    fun isCourierNearPickup(
        courierId: Int,
        requestId: Int,
    ): Boolean

    fun isCourierNearDropOff(
        courierId: Int,
        requestId: Int,
    ): Boolean

    fun itemExistsAtRestaurant(
        item: String,
        restaurantName: String,
    ): Boolean

    fun deleteDeliveryPath(deliveryId: Int): Boolean // for cancelled deliveries or deliveries that were marked as successfully delivered

    fun createDropOffLocation(
        clientId: Int,
        locationId: Int,
    ): Int?

    fun getClientDropOffLocation(clientId: Int): Int?

    fun createItem(
        establishment: String,
        establishmentLocationId: Int,
        designation: String,
        price: Double,
        eta: Long,
    ): Int

    fun clear()
}
