package liftdrop.repository.jdbi

import liftdrop.repository.LocationRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo
import pt.isel.liftdrop.Location

class JdbiLocationRepository(
    private val handle: Handle,
) : LocationRepository {
    override fun createLocation(
        courierId: Int,
        deliveryId: Int,
        location: Location,
    ): Int =
        handle
            .createUpdate(
                """
                INSERT INTO liftdrop.location (latitude, longitude, address)
                VALUES (:latitude, :longitude, :address)
                """,
            ).bind("latitude", location.latitude)
            .bind("longitude", location.longitude)
            .bind("address", location.address!!.id)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

    override fun deleteDeliveryPath(deliveryId: Int): Boolean =
        handle // requires major changes in the db schema in order to be supported
            .createUpdate(
                """
                DELETE FROM liftdrop.location WHERE location_id = :deliveryId
                """,
            ).bind("deliveryId", deliveryId)
            .execute() > 0
}
