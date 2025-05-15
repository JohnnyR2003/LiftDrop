package liftdrop.repository.jdbi

import liftdrop.repository.DeliveryRepository
import org.jdbi.v3.core.Handle
import pt.isel.liftdrop.RequestStatus

class JdbiDeliveryRepository(
    private val handle: Handle,
) : DeliveryRepository {
    /**
     * Updates the delivery status in the database.
     *
     * @param deliveryId The ID of the delivery to update.
     * @param status The new status of the delivery.
     * @return true if the delivery status was updated successfully, false otherwise.
     */
    override fun updateDeliveryStatus(
        deliveryId: Int,
        status: RequestStatus,
    ): Boolean {
        val statusString = status.status.name
        val eta = status.orderETA?.let { "$it milliseconds" }

        return handle
            .createUpdate(
                """
                UPDATE liftdrop.delivery
                SET delivery_status = :status,
                    ETA = CAST(:eta AS BIGINT)
                WHERE delivery_id = :delivery_id
                """.trimIndent(),
            ).bind("status", statusString)
            .bind("eta", eta)
            .bind("delivery_id", deliveryId)
            .execute() > 0
    }

    /**
     * Deletes a delivery from the database.
     *
     * @param deliveryId The ID of the delivery to delete.
     * @return true if the delivery was deleted successfully, false otherwise.
     */
    override fun deleteDelivery(
        courierId: Int,
        deliveryId: Int,
    ): Boolean =
        handle
            .createUpdate(
                """
                DELETE FROM liftdrop.delivery
                WHERE delivery_id = :delivery_id AND courier_id = :courier_id
                """,
            ).bind("delivery_id", deliveryId)
            .bind("courier_id", courierId)
            .execute() > 0

    override fun clear() {
        handle.createUpdate("TRUNCATE TABLE liftdrop.delivery CASCADE;").execute()
    }
}
