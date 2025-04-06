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
                INSERT INTO location (courier_id, delivery_id, latitude, longitude)
                VALUES (:courierId, :deliveryId, :latitude, :longitude)
                """,
            ).bind("courierId", courierId)
            .bind("deliveryId", deliveryId)
            .bind("latitude", location.latitude)
            .bind("longitude", location.longitude)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

    override fun deleteDeliveryPath(deliveryId: Int): Boolean =
        handle
            .createUpdate(
                """
                DELETE FROM location WHERE delivery_id = :deliveryId
                """,
            ).bind("deliveryId", deliveryId)
            .execute() > 0
}
