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

    override fun createDropOffLocation(
        clientId: Int,
        locationId: Int,
    ): Int? =
        handle
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

    override fun getClosestRestaurantLocation(
        restaurantName: String,
        clientLocationId: Int,
    ): Pair<Int, LocationDTO>? {
        val normalizedRestaurantName = "%${restaurantName.trim().replace(" ", "%")}%"
        return handle
            .createQuery(
                """
        SELECT l.location_id AS location_id, l.latitude AS latitude, l.longitude AS longitude
        FROM liftdrop.location l
        JOIN liftdrop.item i ON l.location_id = i.establishment_location
        JOIN liftdrop.location cl ON cl.location_id = :clientLocationId
        WHERE i.establishment ILIKE :restaurant_name
        ORDER BY liftdrop.ST_DistanceSphere(
        liftdrop.ST_MakePoint(cl.longitude, cl.latitude),
        liftdrop.ST_MakePoint(l.longitude, l.latitude)
     )
        LIMIT 1
        """,
            ).bind("restaurant_name", normalizedRestaurantName) // improved partial match
            .bind("clientLocationId", clientLocationId)
            .map { rs, _ ->
                val address = rs.getInt("location_id")
                val location =
                    LocationDTO(
                        latitude = rs.getDouble("latitude"),
                        longitude = rs.getDouble("longitude"),
                    )
                Pair(address, location)
            }.firstOrNull()
    }

    override fun isCourierNearPickup(
        courierId: Int,
        requestId: Int,
    ): Boolean =
        handle
            .createQuery(
                """
                SELECT EXISTS (
                    SELECT 1 FROM liftdrop.courier c
                    JOIN liftdrop.request r ON c.courier_id = r.courier_id
                    JOIN liftdrop.request_details rd ON r.request_id = rd.request_id
                    JOIN liftdrop.location cl ON c.current_location = cl.location_id
                    JOIN liftdrop.location rl ON rd.pickup_location = rl.location_id
                    WHERE c.courier_id = :courierId AND r.request_id = :requestId
                    AND liftdrop.ST_DistanceSphere(
                        liftdrop.ST_MakePoint(cl.longitude, cl.latitude),
                        liftdrop.ST_MakePoint(rl.longitude, rl.latitude)
                    ) <= 100 -- within 100 meters
                )
                """,
            ).bind("courierId", courierId)
            .bind("requestId", requestId)
            .mapTo<Boolean>()
            .first()

    override fun isCourierNearDropOff(
        courierId: Int,
        requestId: Int,
    ): Boolean =
        handle
            .createQuery(
                """
                SELECT EXISTS (
                    SELECT 1 FROM liftdrop.courier c
                    JOIN liftdrop.request r ON c.courier_id = r.courier_id
                    JOIN liftdrop.request_details rd ON r.request_id = rd.request_id
                    JOIN liftdrop.location cl ON c.current_location = cl.location_id
                    JOIN liftdrop.location rl ON rd.dropoff_location = rl.location_id
                    WHERE c.courier_id = :courierId AND r.request_id = :requestId
                    AND liftdrop.ST_DistanceSphere(
                        liftdrop.ST_MakePoint(cl.longitude, cl.latitude),
                        liftdrop.ST_MakePoint(rl.longitude, rl.latitude)
                    ) <= 100 -- within 100 meters
                )
                """,
            ).bind("courierId", courierId)
            .bind("requestId", requestId)
            .mapTo<Boolean>()
            .first()

    override fun itemExistsAtRestaurant(
        item: String,
        restaurantName: String,
    ): Long? {
        val normalizedRestaurantName = "%${restaurantName.trim().replace(" ", "%")}%"
        return handle
            .createQuery(
                """
            SELECT eta FROM liftdrop.item
            WHERE designation = :item AND establishment ILIKE :restaurantName
        """,
            ).bind("item", item)
            .bind("restaurantName", normalizedRestaurantName)
            .mapTo<Long>()
            .firstOrNull()
    }

    override fun getClientDropOffLocation(clientId: Int): Int? =
        handle
            .createQuery(
                """
                SELECT location_id FROM liftdrop.dropoff_spot WHERE client_id = :clientId
                """,
            ).bind("clientId", clientId)
            .mapTo<Int>()
            .firstOrNull()

    override fun createItem(
        establishment: String,
        establishmentLocationId: Int,
        designation: String,
        price: Double,
        eta: Long,
    ): Int =
        handle
            .createUpdate(
                """
            INSERT INTO liftdrop.item (establishment, establishment_location, designation, price, ETA)
            VALUES (:establishment, :establishmentLocation, :designation, :price, :eta)
            """,
            ).bind("establishment", establishment)
            .bind("establishmentLocation", establishmentLocationId)
            .bind("designation", designation)
            .bind("price", price)
            .bind("eta", eta)
            .executeAndReturnGeneratedKeys("item_id") // specify column to get the ID directly
            .mapTo<Int>()
            .one()

    override fun clear() {
        handle.createUpdate("TRUNCATE TABLE liftdrop.location CASCADE;").execute()
    }
}
