package pt.isel.pipeline

class Client(
    override val id: Long,
    override val email: String,
    override val password: String
) : User()