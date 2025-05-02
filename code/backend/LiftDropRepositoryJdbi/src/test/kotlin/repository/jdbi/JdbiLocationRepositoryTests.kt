package repository.jdbi

import liftdrop.repository.jdbi.JdbiLocationRepository
import pt.isel.liftdrop.Address
import pt.isel.pipeline.pt.isel.liftdrop.LocationDTO
import repositoryJdbi.JdbiTestUtils.testWithHandleAndRollback
import kotlin.test.Test

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
                    zipCode = "5",
                )

            // When: creating the location
            val result = locationRepository.createLocation(location, address)

            // Then: the creation should be successful
            assert(result != 0) { "Location should be created successfully" }
        }
    }
}
