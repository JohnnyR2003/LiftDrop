package liftdrop.services

import liftdrop.repository.jdbi.JdbiTransactionManager
import liftdrop.repository.jdbi.configureWithAppRequirements
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.liftdrop.Location
import pt.isel.pipeline.pt.isel.liftdrop.Address
import pt.isel.services.ClientService
import pt.isel.services.CourierService
import pt.isel.services.UserService
import kotlin.math.abs
import kotlin.random.Random

object ServicesTestUtils {
    private val jdbi =
        Jdbi
            .create(
                PGSimpleDataSource().apply {
                    setURL("jdbc:postgresql://localhost:5432/liftdrop?user=postgres&password=postgres")
                },
            ).configureWithAppRequirements()

    fun createCourierService() =
        CourierService(
            JdbiTransactionManager(jdbi),
        )

    fun createClientService() =
        ClientService(
            JdbiTransactionManager(jdbi),
        )

    fun createUsersService() =
        UserService(
            JdbiTransactionManager(jdbi),
        )

    fun newTestId() = abs(Random.nextInt())

    fun newCountry() = "country-${abs(Random.nextLong())}"

    fun newCity() = "city-${abs(Random.nextLong())}"

    fun newStreet() = "street-${abs(Random.nextLong())}"

    fun newFloor() = abs(Random.nextLong())

    fun newZipCode() = "postalCode-${abs(Random.nextLong())}"

    fun newTestEmail() = "email-${abs(Random.nextLong())}@test.com"

    fun newTestPassword() = "password-${abs(Random.nextLong())}"

    fun newTestUserName() = "user-${abs(Random.nextLong())}"

    fun newTestDescription() = "description-${abs(Random.nextLong())}"

    fun newTestAddress() =
        Address(
            id = newTestId(),
            country = newCountry(),
            city = newCity(),
            street = newStreet(),
            streetNumber = newTestId().toString(),
            floor = newFloor().toString(),
            zipCode = newZipCode(),
        )

    fun newTestLocation() =
        Location(
            id = newTestId(),
            latitude = Random.nextDouble(-90.0, 90.0),
            longitude = Random.nextDouble(-180.0, 180.0),
            address = newTestAddress(),
            name = "location-${abs(Random.nextLong())}",
        )

    fun newTokenValidationData() = "token-${abs(Random.nextLong())}"
}
