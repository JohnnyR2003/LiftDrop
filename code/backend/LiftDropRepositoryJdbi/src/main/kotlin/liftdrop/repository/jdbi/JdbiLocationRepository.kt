package liftdrop.repository.jdbi

import liftdrop.repository.LocationRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.liftdrop.Address
import pt.isel.liftdrop.LocationDTO

class JdbiLocationRepository(
    private val handle: Handle,
) : LocationRepository {
    override fun createLocation(
        location: LocationDTO,
        address: Address,
    ): Int {
        val addressId =
            handle
                .createUpdate(
                    """
                    INSERT INTO liftdrop.address (country, city, street, house_number, floor, zip_code)
                    VALUES (:country, :city, :street, :house_number, :floor, :zipCode)
                    """,
                ).bind("country", address.country)
                .bind("city", address.city)
                .bind("street", address.street)
                .bind("house_number", address.streetNumber)
                .bind("floor", address.floor)
                .bind("zipCode", address.zipCode)
                .executeAndReturnGeneratedKeys()
                .mapTo<Int>()
                .one()
        return handle
            .createUpdate(
                """
                INSERT INTO liftdrop.location (latitude, longitude, address)
                VALUES (:latitude, :longitude, :address)
                """,
            ).bind("latitude", location.latitude)
            .bind("longitude", location.longitude)
            .bind("address", addressId)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()
    }

    override fun getLocationById(id: Int): LocationDTO =
        handle
            .createQuery(
                """
                SELECT * FROM liftdrop.location WHERE location_id = :id
                """,
            ).bind("id", id)
            .mapTo<LocationDTO>()
            .findOne()
            .orElseThrow { IllegalArgumentException("Location with id $id not found") }

    override fun deleteDeliveryPath(deliveryId: Int): Boolean =
        handle // requires major changes in the db schema in order to be supported
            .createUpdate(
                """
                DELETE FROM liftdrop.location WHERE location_id = :deliveryId
                """,
            ).bind("deliveryId", deliveryId)
            .execute() > 0

    override fun createDropOffLocation(clientId: Int, locationId: Int): Int? {
        return handle
            .createUpdate(
                """
                INSERT INTO liftdrop.dropoff_spot(location_id, client_id)
                VALUES (:locationId, :clientId)
                """,
            ).bind("clientId", clientId)
            .bind("locationId", locationId)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .singleOrNull()

    }

    override fun getRestaurantLocationByItem(
        item: String,
        restaurantName: String,
    ): LocationDTO =
        handle
            .createQuery(
                """
                SELECT l.latitude, l.longitude
                FROM liftdrop.location l
                JOIN liftdrop.item i ON l.location_id = i.establishment_location
                WHERE i.designation = :item AND i.establishment = :restaurant_name
                """,
            ).bind("item", item)
            .bind("restaurant_name", restaurantName)
            .mapTo<LocationDTO>()
            .first()

    override fun clear() {
        handle.createUpdate("TRUNCATE TABLE liftdrop.location CASCADE;").execute()
    }
}
