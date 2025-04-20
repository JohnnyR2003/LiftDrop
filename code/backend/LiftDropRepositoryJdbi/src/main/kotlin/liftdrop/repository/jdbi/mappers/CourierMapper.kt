package liftdrop.repository.jdbi.mappers

import org.jdbi.v3.core.mapper.RowMapper
import pt.isel.liftdrop.Courier
import pt.isel.liftdrop.User
import pt.isel.liftdrop.UserRole

class CourierMapper : RowMapper<Courier> {
    override fun map(
        rs: java.sql.ResultSet,
        ctx: org.jdbi.v3.core.statement.StatementContext,
    ): Courier {
        val user = User(
            id = rs.getInt("courier_id"),
            email = rs.getString("email"),
            password = rs.getString("password"),
            name = rs.getString("name"),
            role = UserRole.valueOf(rs.getString("role")),
        )
        return Courier(
            user,
            rs.getInt("current_location"),
            rs.getBoolean("is_available"),
        )
    }
}
