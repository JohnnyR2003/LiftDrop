package liftdrop.repository.jdbi

import liftdrop.repository.DeliveryRepository
import org.jdbi.v3.core.Handle
import pt.isel.liftdrop.Status

class JdbiDeliveryRepository(
    private val handle: Handle,
) : DeliveryRepository {
    override fun createDelivery(
        requestId: Int,
        startTime: String,
        endTime: String,
    ): Int {
        TODO("Not yet implemented")
    }

    override fun updateDeliveryStatus(
        deliveryId: Int,
        status: Status,
    ): Boolean {
        TODO("Not yet implemented")
    }

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
