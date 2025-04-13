package liftdrop.repository.jdbi.mappers

import org.jdbi.v3.core.mapper.RowMapper
import pt.isel.liftdrop.RequestStatus
import pt.isel.liftdrop.Status
import pt.isel.pipeline.pt.isel.liftdrop.Request
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
                    orderETA = rs.getLong("eta"),
                ),
            createdAt = rs.getInt("created_at"),
            details =
                RequestDetails(
                    restaurantName = rs.getString("eta"),
                    description = rs.getString("description"),
                    pickupLocation = rs.getInt("delivery_id"),
                    dropOffLocation = rs.getInt("pickup_id"),
                ),
        )
}
