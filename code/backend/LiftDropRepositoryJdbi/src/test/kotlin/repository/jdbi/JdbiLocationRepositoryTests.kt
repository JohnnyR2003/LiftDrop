package repository.jdbi

import liftdrop.repository.jdbi.JdbiLocationRepository
import pt.isel.liftdrop.Location
import pt.isel.pipeline.pt.isel.liftdrop.Address
import repositoryJdbi.JdbiTestUtils.testWithHandleAndRollback
import kotlin.test.Test

class JdbiLocationRepositoryTests {
    @Test
    fun `location is created successfully`() {
        testWithHandleAndRollback { handle ->
            // Given: repositories for location operations
            val locationRepository = JdbiLocationRepository(handle)

            val courierId = 4
            val deliveryId = 1

            // Given: a new location
            val location =
                Location(
                    id = 1,
                    latitude = 40.7128,
                    longitude = -74.0060,
                    address =
                        Address(
                            id = 1,
                            country = "USA",
                            city = "New York",
                            street = "Main St",
                            streetNumber = "123",
                            floor = "1",
                            zipCode = "10001",
                        ),
                    name = "Test Location",
                )

            // When: creating the location
            val result = locationRepository.createLocation(courierId, deliveryId, location)

            // Then: the creation should be successful
            assert(result != 0) { "Location should be created successfully" }
        }
    }
}
