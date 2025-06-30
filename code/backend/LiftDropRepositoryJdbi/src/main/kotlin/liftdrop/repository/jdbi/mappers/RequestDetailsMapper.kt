package liftdrop.repository.jdbi.mappers

import org.jdbi.v3.core.mapper.RowMapper
import pt.isel.liftdrop.LocationDTO
import pt.isel.liftdrop.RequestDetailsDTO

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
            pickupAddress =
                rs.getString("pickup_street") +
                    ", " +
                    rs.getString("pickup_street_number") +
                    ", " +
                    rs.getString("pickup_postal_code"),
            dropoffLocation =
                LocationDTO(
                    latitude = rs.getDouble("dropoff_latitude"),
                    longitude = rs.getDouble("dropoff_longitude"),
                ),
            dropoffAddress =
                rs.getString("dropoff_street") +
                    ", " +
                    rs.getString("dropoff_street_number") +
                    ", " +
                    rs.getString("dropoff_postal_code"),
            price = rs.getString("price"),
            item = rs.getString("item"),
            quantity = rs.getInt("quantity"),
        )
}
