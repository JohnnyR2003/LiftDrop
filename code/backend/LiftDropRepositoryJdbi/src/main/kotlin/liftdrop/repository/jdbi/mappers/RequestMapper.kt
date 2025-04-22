package liftdrop.repository.jdbi.mappers

import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.RowMapper
import pt.isel.liftdrop.RequestStatus
import pt.isel.liftdrop.Status
import pt.isel.liftdrop.Request
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
                    orderETA = rs.getLong("eta_seconds"),
                ),
            createdAt = Instant.fromEpochMilliseconds(rs.getTimestamp("created_at").time),
            details =
                RequestDetails(
                    restaurantName = rs.getString("restaurant_name") ?: "",  // Changed from "eta" to "restaurant_name"
                    description = rs.getString("description"),
                    pickupLocation = rs.getInt("pickup_location"),    // Changed from "delivery_id" to "pickup_location"
                    dropOffLocation = rs.getInt("dropoff_location"),  // Changed from "pickup_id" to "dropoff_location"
                ),
        )
}