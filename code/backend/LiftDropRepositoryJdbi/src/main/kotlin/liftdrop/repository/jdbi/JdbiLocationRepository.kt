package liftdrop.repository.jdbi

import liftdrop.repository.LocationRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.liftdrop.Address
import pt.isel.pipeline.pt.isel.liftdrop.LocationDTO

class JdbiLocationRepository(
    private val handle: Handle,
) : LocationRepository {
    override fun createLocation(
        location: LocationDTO,
        address: Address,
    ): Int =
        handle
            .createUpdate(
                """
                INSERT INTO liftdrop.location (latitude, longitude, address)
                VALUES (:latitude, :longitude, :address)
                """,
            ).bind("latitude", location.latitude)
            .bind("longitude", location.longitude)
            .bind("address", address.zipCode.toInt())
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

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
}
