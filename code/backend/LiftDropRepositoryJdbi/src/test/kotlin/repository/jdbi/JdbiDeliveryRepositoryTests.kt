package repository.jdbi

import liftdrop.repository.jdbi.JdbiCourierRepository
import liftdrop.repository.jdbi.JdbiDeliveryRepository
import pt.isel.liftdrop.RequestStatus
import pt.isel.liftdrop.Status
import repositoryJdbi.JdbiTestUtils.testWithHandleAndRollback
import kotlin.test.Test

class JdbiDeliveryRepositoryTests {
    @Test
    fun `delivery is updated successfully and is deleted afterwards`() {
        testWithHandleAndRollback { handle ->
            // Given: repositories for delivery operations
            val deliveryRepository = JdbiDeliveryRepository(handle)
            val courierRepository = JdbiCourierRepository(handle)

            val courierId = 4
            val requestId = 3
            // Given: a new delivery resulting from an accepted request
            courierRepository.acceptRequest(requestId, courierId)
            // Given: the delivery ID and the new status
            val deliveryId = 1
            val newStatus =
                RequestStatus(
                    status = Status.IN_PROGRESS,
                    orderETA = null,
                )

            // When: updating the delivery status
            val result = deliveryRepository.updateDeliveryStatus(deliveryId, newStatus)

            // Then: the update should be successful
            assert(result) { "Delivery status should be updated successfully" }

            // When: deleting the delivery

            val deleteResult = deliveryRepository.deleteDelivery(courierId, deliveryId)

            // Then: the deletion should be successful

            assert(deleteResult) { "Delivery should be deleted successfully" }
        }
    }
}
