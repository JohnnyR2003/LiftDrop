package liftdrop.repository.jdbi.mappers

import org.jdbi.v3.core.mapper.RowMapper
import pt.isel.liftdrop.LocationDTO

class LocationDTOMapper : RowMapper<LocationDTO> {
    override fun map(
        r: java.sql.ResultSet?,
        ctx: org.jdbi.v3.core.statement.StatementContext?,
    ): LocationDTO? =
        if (r != null) {
            LocationDTO(
                r.getDouble("latitude"),
                r.getDouble("longitude"),
            )
        } else {
            null
        }
}
