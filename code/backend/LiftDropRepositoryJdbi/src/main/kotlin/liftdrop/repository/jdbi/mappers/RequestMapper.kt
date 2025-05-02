package liftdrop.repository.jdbi.mappers

import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.RowMapper
import pt.isel.liftdrop.Request
import pt.isel.liftdrop.RequestStatus
import pt.isel.liftdrop.Status
import pt.isel.pipeline.pt.isel.liftdrop.RequestDetails

class RequestMapper : RowMapper<Request> {
    override fun map(
        rs: java.sql.ResultSet,
        ctx: org.jdbi.v3.core.statement.StatementContext,
    ): Request =
        Request(
            id = rs.getInt("request_id"),
            clientId = rs.getInt("client_id"),
            courierId = rs.getInt("courier_id"),
            requestStatus =
                RequestStatus(
                    status = Status.valueOf(rs.getString("request_status")),
                    orderETA = rs.getLong("order_eta"),
                ),
            createdAt = Instant.fromEpochMilliseconds(rs.getLong("created_at")),
            details =
                RequestDetails(
                    restaurantName = rs.getString("restaurant_name") ?: "Unknown",
                    description = rs.getString("description"),
                    pickupLocation = rs.getInt("pickup_location"),
                    dropoffLocation = rs.getInt("dropoff_location"),
                ),
        )
}
