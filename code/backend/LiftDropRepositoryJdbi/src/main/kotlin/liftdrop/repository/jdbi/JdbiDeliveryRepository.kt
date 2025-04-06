package liftdrop.repository.jdbi

import liftdrop.repository.DeliveryRepository
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class JdbiDeliveryRepository(
    private val handle: Handle,
) : DeliveryRepository {
    override fun createDelivery(
        requestId: Int,
        startTime: String,
        endTime: String,
        deliveryStatus: String,
    ): Int =
        handle
            .createUpdate(
                """
                INSERT INTO delivery (request_id, start_time, end_time, delivery_status)
                VALUES (:requestId, :startTime, :endTime, :deliveryStatus)
                """,
            ).bind("requestId", requestId)
            .bind("startTime", startTime)
            .bind("endTime", endTime)
            .bind("deliveryStatus", deliveryStatus)
            .executeAndReturnGeneratedKeys()
            .mapTo<Int>()
            .one()

    override fun completeDelivery(deliveryId: Int): Boolean =
        handle
            .createUpdate(
                """
                UPDATE delivery
                SET delivery_status = 'completed'
                WHERE id = :deliveryId
                """,
            ).bind("deliveryId", deliveryId)
            .execute() > 0

    override fun deleteDelivery(deliveryId: Int): Boolean =
        handle
            .createUpdate(
                """
                DELETE FROM delivery
                WHERE id = :deliveryId
                """,
            ).bind("deliveryId", deliveryId)
            .execute() > 0
}
