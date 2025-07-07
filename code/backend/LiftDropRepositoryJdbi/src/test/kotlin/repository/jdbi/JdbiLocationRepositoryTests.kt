package repository.jdbi

import liftdrop.repository.jdbi.JdbiLocationRepository
import pt.isel.liftdrop.Address
import pt.isel.liftdrop.LocationDTO
import repositoryJdbi.JdbiTestUtils.testWithHandleAndRollback
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JdbiLocationRepositoryTests {
    @Test
    fun `location is created successfully`() {
        testWithHandleAndRollback { handle ->
            // Given: repositories for location operations
            val locationRepository = JdbiLocationRepository(handle)

            // Given: a new location
            val location =
                LocationDTO(
                    latitude = 40.7128,
                    longitude = -74.0060,
                )

            val address =
                Address(
                    country = "USA",
                    city = "New York",
                    street = "5th Avenue",
                    streetNumber = "123",
                    floor = "1st",
                    zipCode = "3",
                )

            // When: creating the location
            val result = locationRepository.createLocation(location, address)

            // Then: the creation should be successful
            assert(result != 0) { "Location should be created successfully" }
        }
    }

    @Test
    fun `partial restaurant name search returns closest location from existing data`() {
        testWithHandleAndRollback { handle ->
            val repo = JdbiLocationRepository(handle)

            // Use client location 1 (Avenida de Roma)
            val clientLocationId = 1

            // Test partial search for "DONALDS" (should match MC DONALDS Roma at location 1)
            val resultDonalds = repo.getClosestRestaurantLocation("MC DONALDS", clientLocationId)
            assertNotNull(resultDonalds)
            assertEquals(1, resultDonalds.first) // location_id 1

            // Test partial search for "BURGER" (should match BURGER KING MARQUÊS at location 2)
            val resultBurger = repo.getClosestRestaurantLocation("BURGER KING", clientLocationId)
            assertNotNull(resultBurger)
            assertEquals(2, resultBurger.first) // location_id 2

            // Test partial search for "KFC" (should match KFC COLOMBO at location 4)
            val resultKfc = repo.getClosestRestaurantLocation("KFC", clientLocationId)
            assertNotNull(resultKfc)
            assertEquals(4, resultKfc.first) // location_id 4
        }
    }
}
