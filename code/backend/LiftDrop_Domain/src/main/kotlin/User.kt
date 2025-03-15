package pt.isel.pipeline

sealed class User (
   val id: Long,
   val email: String,
   val password: String
)

class Client(
    id: Long,
    email: String,
    password: String,
    val name: String,
) : User(id, email, password)