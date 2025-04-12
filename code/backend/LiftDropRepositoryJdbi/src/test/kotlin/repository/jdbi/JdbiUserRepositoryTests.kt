package repository.jdbi

import liftdrop.repository.jdbi.JdbiUserRepository
import pt.isel.liftdrop.UserRole
import repositoryJdbi.JdbiTestUtils.newTestEmail
import repositoryJdbi.JdbiTestUtils.newTestPassword
import repositoryJdbi.JdbiTestUtils.newTestUserName
import repositoryJdbi.JdbiTestUtils.testWithHandleAndRollback
import kotlin.test.Test

class JdbiUserRepositoryTests {
    @Test
    fun `should create user account and successfully find it by both name and email and finally delete it successfully`() {
        testWithHandleAndRollback { handle ->
            // Given: a user repository
            val userRepository = JdbiUserRepository(handle)

            // Given: user information for creating a new user
            val userEmail = newTestEmail()
            val userPassword = newTestPassword()
            val userName = newTestUserName()
            val role = UserRole.CLIENT

            // When: creating a new user
            val userCreation = userRepository.createUser(userEmail, userPassword, userName, role)

            if (userCreation == 0) {
                throw Exception("User should be created")
            } else {
                // Then: the user should be retrievable from the database
                val user = userRepository.findUserByEmail(userEmail) ?: throw Exception("User should be created")

                // When: finding the user by name
                val foundUserByName = userRepository.findUserByName(userName)

                // Then: the found user should match the expected user
                assert(foundUserByName != null) { "User should be found by name" }
                assert(foundUserByName?.email == userEmail) { "Found user's email should match" }

                // When: deleting the user
                val deleteResult = userRepository.deleteUser(userEmail)

                // Then: the delete operation should be successful
                assert(deleteResult == 1) { "User should be deleted successfully" }

                // When: trying to find the deleted user by email
                val deletedUser = userRepository.findUserByEmail(userEmail)

                // Then: the deleted user should not be found
                assert(deletedUser == null) { "Deleted user should not be found" }
            }
        }
    }
}
