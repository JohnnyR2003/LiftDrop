package repository.jdbi

import liftdrop.repository.jdbi.JdbiRequestRepository
import repositoryJdbi.JdbiTestUtils.testWithHandleAndRollback
import kotlin.test.Test

class JdbiRequestRepositoryTests {
    @Test
    fun `should create request successfully`() {
        testWithHandleAndRollback { handle ->
            // Given: a request repository
            val requestRepository = JdbiRequestRepository(handle)

            // Given: request information
            val clientId = 3
            val eta = 1L

            // When: creating a new request
            val requestId = requestRepository.createRequest(clientId, eta)

            // Then: the request should be created successfully
            assert(requestId > 0) { "Request ID should be greater than 0" }
        }
    }
}
