package liftdrop.services

import liftdrop.services.ServicesTestUtils.createGeocodingService
import pt.isel.liftdrop.CourierWithLocation
import kotlin.test.Test
import kotlin.test.assertEquals

class CourierAssignmentTest {
    @Test
    fun `rankCouriersByScore should rank couriers by combined travel time and rating`() {
        val geocodingServices = createGeocodingService()

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

        val rankedCouriers = geocodingServices.rankCouriersByScore(couriers)

        assertEquals(3, rankedCouriers[0].courierId) // Best score: high rating, low travel time
        assertEquals(1, rankedCouriers[1].courierId)
        assertEquals(2, rankedCouriers[2].courierId) // Worst score: low rating, high travel time
    }
}
