package liftdrop.repository.jdbi.mappers

import org.jdbi.v3.core.mapper.RowMapper
import pt.isel.liftdrop.Client

class ClientMapper : RowMapper<Client> {
    override fun map(
        r: java.sql.ResultSet?,
        ctx: org.jdbi.v3.core.statement.StatementContext?,
    ): Client? =
        if (r != null) {
            Client(
                r.getInt("client_id"),
                r.getInt("address"),
            )
        } else {
            null
        }
}
