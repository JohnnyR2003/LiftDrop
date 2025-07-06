package liftdrop.services

import liftdrop.services.ServicesTestUtils.createAssignmentService
import pt.isel.liftdrop.CourierWithLocation
import kotlin.test.Test
import kotlin.test.assertEquals

class CourierAssignmentTest {
    @Test
    fun `rankCouriersByScore should rank couriers by combined travel time and rating`() {
        val assignmentService = createAssignmentService()

        val couriers =
            listOf(
                CourierWithLocation(
                    courierId = 1,
                    latitude = 38.736946,
                    longitude = -9.142685,
                    distanceMeters = 1000.0,
                    rating = 4.0,
                    estimatedTravelTime = 300L,
                ),
                CourierWithLocation(
                    courierId = 2,
                    latitude = 38.736000,
                    longitude = -9.140000,
                    distanceMeters = 2000.0,
                    rating = 3.0,
                    estimatedTravelTime = 600L,
                ),
                CourierWithLocation(
                    courierId = 3,
                    latitude = 38.737500,
                    longitude = -9.143000,
                    distanceMeters = 1200.0,
                    rating = 5.0,
                    estimatedTravelTime = 350L,
                ),
            )

        val rankedCouriers = assignmentService.rankCouriersByScore(couriers)

        assertEquals(3, rankedCouriers[0].courierId) // Best score: high rating, low travel time
        assertEquals(1, rankedCouriers[1].courierId)
        assertEquals(2, rankedCouriers[2].courierId) // Worst score: low rating, high travel time
    }

//    @Test
//    fun `should return true when courier accepts assignment`() {
//        val assignmentService = createAssignmentService()
//    }
//
//    @Test
//    fun `should return false when all couriers decline or timeout`() {
//        // Arrange: mock fetchRankedCouriersByTravelTime to return couriers
//        // Mock AssignmentCoordinator to complete with false or timeout
//        // Assert: handleCourierAssignment returns false after all attempts
//    }
//
//    @Test
//    fun `should retry with increased distance if no courier accepts`() {
//        // Arrange: mock fetchRankedCouriersByTravelTime to return empty at first, then couriers
//        // Assert: handleCourierAssignment retries and eventually returns expected result
//    }
//
//    @Test
//    fun `should handle no couriers found and retry after delay`() {
//        // Arrange: mock fetchRankedCouriersByTravelTime to always return Either.Left
//        // Assert: handleCourierAssignment retries as expected
//    }
//
//    @Test
//    fun `should handle TimeoutCancellationException and continue to next courier`() {
//        // Arrange: mock AssignmentCoordinator.await to throw TimeoutCancellationException
//        // Assert: handleCourierAssignment continues to next courier
//    }
//
//    @Test
//    fun `should handle CancellationException and continue to next courier`() {
//        // Arrange: mock AssignmentCoordinator.await to throw CancellationException
//        // Assert: handleCourierAssignment continues to next courier
//    }
//
//    @Test
//    fun `should handle request details not found in transaction and skip courier`() {
//        // Arrange: mock transactionManager to return null for request details
//        // Assert: handleCourierAssignment skips to next courier
//    }
//
//    @Test
//    fun `should rank couriers by travel time and score correctly`() {
//        // Arrange: provide couriers with different travel times and ratings
//        // Assert: rankCouriersByTravelTime and rankCouriersByScore return expected order
//    }
//
//    @Test
//    fun `should estimate courier earnings correctly`() {
//        // Arrange: provide known values
//        // Assert: estimateCourierEarnings returns expected result
//    }
}
