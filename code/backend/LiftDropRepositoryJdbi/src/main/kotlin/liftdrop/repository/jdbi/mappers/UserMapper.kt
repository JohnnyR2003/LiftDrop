package liftdrop.repository.jdbi.mappers

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.liftdrop.User
import pt.isel.liftdrop.UserRole
import java.sql.ResultSet

class UserMapper : RowMapper<User> {
    override fun map(
        r: ResultSet?,
        ctx: StatementContext?,
    ): User? =
        if (r != null) {
            User(
                r.getInt("user_id"),
                r.getString("email"),
                r.getString("password"),
                r.getString("name"),
                UserRole.valueOf(r.getString("role")),
            )
        } else {
            null
        }
}
