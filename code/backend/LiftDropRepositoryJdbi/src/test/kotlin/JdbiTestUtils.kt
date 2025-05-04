package repositoryJdbi

import EnvironmentTest.getDbUrl
import liftdrop.repository.jdbi.configureWithAppRequirements
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.liftdrop.Address
import pt.isel.liftdrop.LocationDTO
import kotlin.math.abs
import kotlin.random.Random

object JdbiTestUtils {
    private val jdbi =
        Jdbi
            .create(
                PGSimpleDataSource().apply {
                    setURL(getDbUrl())
                },
            ).configureWithAppRequirements()

    fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

    fun testWithHandleAndRollback(block: (Handle) -> Unit) =
        jdbi.useTransaction<Exception> { handle ->
            block(handle)
            handle.rollback()
        }

    fun newTestId() = abs(Random.nextInt())

    fun newTestEmail() = "email-${abs(Random.nextLong())}@test.com"

    fun newTestPassword() = "password-${abs(Random.nextLong())}"

    fun newTestUserName() = "user-${abs(Random.nextLong())}"

    fun newTestAddress() =
        Address(
            country = "country-${abs(Random.nextLong())}",
            city = "city-${abs(Random.nextLong())}",
            street = "street-${abs(Random.nextLong())}",
            streetNumber = newTestId().toString(),
            floor = "floor-${abs(Random.nextLong())}",
            zipCode = "postalCode-${abs(Random.nextLong())}",
        )

    fun newTestLocation() =
        LocationDTO(
            latitude = Random.nextDouble(-90.0, 90.0),
            longitude = Random.nextDouble(-180.0, 180.0),
        )

    fun newTokenValidationData() = "token-${abs(Random.nextLong())}"
}
