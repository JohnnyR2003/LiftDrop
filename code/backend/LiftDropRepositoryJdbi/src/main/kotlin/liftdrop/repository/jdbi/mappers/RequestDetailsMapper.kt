package liftdrop.repository.jdbi.mappers

import org.jdbi.v3.core.mapper.RowMapper
import pt.isel.liftdrop.RequestDetailsDTO
import pt.isel.pipeline.pt.isel.liftdrop.LocationDTO

class RequestDetailsMapper : RowMapper<RequestDetailsDTO> {
    override fun map(
        rs: java.sql.ResultSet,
        ctx: org.jdbi.v3.core.statement.StatementContext,
    ): RequestDetailsDTO =
        RequestDetailsDTO(
            requestId = rs.getInt("request_id"),
            pickupLocation =
                LocationDTO(
                    latitude = rs.getDouble("pickup_latitude"),
                    longitude = rs.getDouble("pickup_longitude"),
                ),
            dropoffSpot =
                LocationDTO(
                    latitude = rs.getDouble("dropoff_latitude"),
                    longitude = rs.getDouble("dropoff_longitude"),
                ),
            description = rs.getString("description"),
        )
}
