package liftdrop.repository.jdbi

import liftdrop.repository.jdbi.mappers.*
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
    registerRowMapper(CourierMapper())
    registerRowMapper(RequestMapper())
    registerRowMapper(RequestDetailsMapper())
    registerRowMapper(LocationDTOMapper())

    return this
}
