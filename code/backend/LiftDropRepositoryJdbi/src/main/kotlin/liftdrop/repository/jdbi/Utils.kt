package liftdrop.repository.jdbi

import liftdrop.repository.jdbi.mappers.AddressMapper
import liftdrop.repository.jdbi.mappers.ClientMapper
import liftdrop.repository.jdbi.mappers.CourierMapper
import liftdrop.repository.jdbi.mappers.LocationMapper
import liftdrop.repository.jdbi.mappers.UserMapper
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin

fun Jdbi.configureWithAppRequirements(): Jdbi {
    installPlugin(KotlinPlugin())
    installPlugin(PostgresPlugin())

    registerRowMapper(UserMapper())
    registerRowMapper(CourierMapper())
    registerRowMapper(AddressMapper())
    registerRowMapper(LocationMapper())
    registerRowMapper(ClientMapper())

    return this
}
