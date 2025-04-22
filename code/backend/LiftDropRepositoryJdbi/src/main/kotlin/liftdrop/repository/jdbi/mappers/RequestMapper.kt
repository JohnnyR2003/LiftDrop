package liftdrop.repository.jdbi.mappers

import org.jdbi.v3.core.mapper.RowMapper
import pt.isel.liftdrop.Status
import pt.isel.liftdrop.RequestDTO

class RequestMapper : RowMapper<RequestDTO> {
    override fun map(
        rs: java.sql.ResultSet,
        ctx: org.jdbi.v3.core.statement.StatementContext,
    ): RequestDTO =
        RequestDTO(
            id = rs.getInt("request_id"),
            clientId = rs.getInt("client_id"),
            courierId = rs.getInt("courier_id"),
            requestStatus = Status.valueOf(rs.getString("request_status")),
        )
}
