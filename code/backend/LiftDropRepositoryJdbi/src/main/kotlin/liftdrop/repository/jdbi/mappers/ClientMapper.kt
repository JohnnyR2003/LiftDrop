package liftdrop.repository.jdbi.mappers

import org.jdbi.v3.core.mapper.RowMapper
import pt.isel.liftdrop.Client
import pt.isel.liftdrop.User
import pt.isel.liftdrop.UserRole

class ClientMapper : RowMapper<Client> {
    override fun map(
        r: java.sql.ResultSet?,
        ctx: org.jdbi.v3.core.statement.StatementContext?,
    ): Client? =
        if (r != null) {
            val user = User(
                id = r.getInt("client_id"),
                email = r.getString("email"),
                password = r.getString("password"),
                name = r.getString("name"),
                role = UserRole.valueOf(r.getString("role")),
            )
            Client(
                user,
                r.getInt("address").takeIf { !r.wasNull() },
            )
        } else {
            null
        }
}
