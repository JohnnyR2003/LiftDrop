package liftdrop.repository.jdbi.mappers

import org.jdbi.v3.core.mapper.RowMapper
import pt.isel.liftdrop.Address
import pt.isel.liftdrop.Location

class LocationMapper : RowMapper<Location> {
    override fun map(
        r: java.sql.ResultSet?,
        ctx: org.jdbi.v3.core.statement.StatementContext?,
    ): Location? =
        if (r != null) {
            Location(
                r.getInt("location_id"),
                r.getDouble("latitude"),
                r.getDouble("longitude"),
                Address(
                    r.getInt("address_id"),
                    r.getString("country"),
                    r.getString("city"),
                    r.getString("street"),
                    r.getString("house_number"),
                    r.getString("floor"),
                    r.getString("zip_code"),
                ),
                r.getString("name"),
            )
        } else {
            null
        }
}
