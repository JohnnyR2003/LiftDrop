package pt.isel.services

import com.example.utils.Either
import com.example.utils.failure
import com.example.utils.success
import jakarta.inject.Named
import liftdrop.repository.TransactionManager
import pt.isel.liftdrop.Address
import pt.isel.liftdrop.LocationDTO
import pt.isel.services.client.toFormattedString
import pt.isel.services.google.GeocodingServices
import pt.isel.services.user.LocationError

@Named
class LocationServices(
    private val transactionManager: TransactionManager,
    private val geocodingServices: GeocodingServices,
) {
    fun addPickUpLocation(
        address: Address,
        item: String,
        restaurantName: String,
        price: Double,
        eta: Long,
    ): Either<LocationError, Int> {
        val loc =
            geocodingServices.getLatLngFromAddress(address.toFormattedString())
                ?: return failure(LocationError.InvalidAddress)

        return transactionManager.run {
            val locId =
                it.locationRepository
                    .createLocation(
                        LocationDTO(loc.first, loc.second),
                        address,
                    )

            val itemId =
                it.locationRepository
                    .createItem(
                        establishment = restaurantName,
                        establishmentLocationId = locId,
                        designation = item,
                        price = price,
                        eta = eta,
                    )

            return@run success(itemId)
        }
    }
}
