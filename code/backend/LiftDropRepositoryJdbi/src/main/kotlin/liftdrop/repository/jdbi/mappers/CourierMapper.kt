package liftdrop.repository.jdbi.mappers

import org.jdbi.v3.core.mapper.RowMapper
import pt.isel.liftdrop.Courier

class CourierMapper : RowMapper<Courier> {
    override fun map(
        rs: java.sql.ResultSet,
        ctx: org.jdbi.v3.core.statement.StatementContext,
    ): Courier =
        Courier(
            rs.getInt("courier_id"),
            rs.getInt("current_location"),
            rs.getBoolean("is_available"),
        )
}
