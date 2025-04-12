package liftdrop.repository.jdbi.mappers

import org.jdbi.v3.core.mapper.RowMapper
import pt.isel.pipeline.pt.isel.liftdrop.Address

class AddressMapper : RowMapper<Address> {
    override fun map(
        r: java.sql.ResultSet?,
        ctx: org.jdbi.v3.core.statement.StatementContext?,
    ): Address? =
        if (r != null) {
            Address(
                r.getInt("address_id"),
                r.getString("country"),
                r.getString("city"),
                r.getString("street"),
                r.getString("street_number"),
                r.getString("floor"),
                r.getString("zip_code"),
            )
        } else {
            null
        }
}
