package liftdrop.services

import com.example.utils.Either
import liftdrop.services.ServicesTestUtils.createClientService
import liftdrop.services.ServicesTestUtils.createCourierService
import liftdrop.services.ServicesTestUtils.newTestAddress
import liftdrop.services.ServicesTestUtils.newTestEmail
import liftdrop.services.ServicesTestUtils.newTestLocation
import liftdrop.services.ServicesTestUtils.newTestPassword
import liftdrop.services.ServicesTestUtils.newTestUserName
import org.junit.jupiter.api.BeforeAll
import pt.isel.liftdrop.User
import pt.isel.liftdrop.UserRole
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

class CourierServiceTests {
    companion object {
        var user1: User? = null
        var address = newTestAddress()
        var cId = 0

        @JvmStatic
        @BeforeAll
        fun setup() {
            val clientService = createClientService()

            val testEmail = newTestEmail()
            val testPassword = newTestPassword()
            val testUserName = newTestUserName()
            val testAddress = newTestAddress()

            val clientCreation =
                clientService.registerClient(
                    testEmail,
                    testPassword,
                    testUserName,
                    testAddress,
                )

            if (clientCreation is Either.Left) fail("Unexpected error")

            user1 =
                User(
                    id = (clientCreation as Either.Right).value,
                    email = testEmail,
                    password = testPassword,
                    name = testUserName,
                    UserRole.CLIENT,
                )

            address = testAddress
            cId = (clientCreation).value
        }

//        @JvmStatic
//        @AfterAll
//        fun tearDown() {
//        }
    }

    @Test
    fun `shouldn't register courier with already existing email`() {
        val courierService = createCourierService()

        val courierEmail = newTestEmail()

        val co1 =
            courierService.registerCourier(
                courierEmail,
                newTestPassword(),
                newTestUserName(),
                newTestLocation(),
            )

        assertTrue(co1 is Either.Right)

        val co2 =
            courierService.registerCourier(
                courierEmail,
                newTestPassword(),
                newTestUserName(),
                newTestLocation(),
            )

        assertTrue(co2 is Either.Left)
    }

    @Test
    fun `should register and login courier successfully`() {
        val courierService = createCourierService()

        val courierEmail = newTestEmail()
        val courierPassword = newTestPassword()

        val courierRegistration =
            courierService.registerCourier(
                courierEmail,
                courierPassword,
                newTestUserName(),
                newTestLocation(),
            )

        val courierLogin =
            courierService.loginCourier(
                courierEmail,
                courierPassword,
            )
        println(courierLogin)
        assertTrue(courierRegistration is Either.Right)
        assertTrue(courierLogin is Either.Right)
    }

    @Test
    fun `courier should not login with wrong password`() {
        val courierService = createCourierService()

        val courierEmail = newTestEmail()
        val courierPassword = newTestPassword()

        val courierRegistration =
            courierService.registerCourier(
                courierEmail,
                courierPassword,
                newTestUserName(),
                newTestLocation(),
            )

        if (courierRegistration is Either.Left) fail("Unexpected error")

        val wrongPassword = "wrongPassword"

        val loginResult =
            courierService.loginCourier(
                courierEmail,
                wrongPassword,
            )

        assertTrue(loginResult is Either.Left)
    }

    @Test
    fun `courier should accept request successfully`() {
        val courierService = createCourierService()

        val courierEmail = newTestEmail()
        val courierPassword = newTestPassword()

        val courierRegistration =
            courierService.registerCourier(
                courierEmail,
                courierPassword,
                newTestUserName(),
                newTestLocation(),
            )

        if (courierRegistration is Either.Left) fail("Unexpected error")

        val requestId = 4 // Assuming you have a valid request ID to accept

        val acceptRequestResult =
            courierService.acceptRequest(
                (courierRegistration as Either.Right).value,
                requestId,
            )

        assertTrue(acceptRequestResult is Either.Right)
    }

    @Test
    fun `courier should not accept request with wrong id`() {
        val courierService = createCourierService()

        val courierEmail = newTestEmail()
        val courierPassword = newTestPassword()

        val courierRegistration =
            courierService.registerCourier(
                courierEmail,
                courierPassword,
                newTestUserName(),
                newTestLocation(),
            )

        if (courierRegistration is Either.Left) fail("Unexpected error")

        val requestId = 999 // Inexistent request ID

        val acceptRequestResult =
            courierService.acceptRequest(
                (courierRegistration as Either.Right).value,
                requestId,
            )

        assertTrue(acceptRequestResult is Either.Left)
    }

   /* @Test
    fun `courier should deliver package successfully`() {
        val courierService = createCourierService()
        val clientService = createClientService()

        val courierEmail = newTestEmail()
        val courierPassword = newTestPassword()

        val courierRegistration =
            courierService.registerCourier(
                courierEmail,
                courierPassword,
                newTestUserName(),
                newTestLocation(),
            )

        if (courierRegistration is Either.Left) fail("Unexpected error")

        // A client makes a request

        val requestCreation =
            clientService.makeRequest(
                Client(
                    user = user1!!,
                    address = address.id,
                ), // Assuming you have a valid client ID
                description = "Test package",
                pickupLocationId = 1, // Assuming you have a valid pickup location ID
                dropOffLocationId = 2, // Assuming you have a valid drop-off location ID
            )

        val requestId = 1

        val acceptRequestResult =
            courierService.acceptRequest(
                (courierRegistration as Either.Right).value,
                requestId,
            )

        if (acceptRequestResult is Either.Left) fail("Unexpected error")

        val packageId = 1 // Assuming you have a valid package ID to deliver

        println(courierRegistration)
        val deliverResult =
            courierService.deliver(
                courierRegistration.value,
                packageId,
            )

        assertTrue(deliverResult is Either.Right)
    }*/
}
