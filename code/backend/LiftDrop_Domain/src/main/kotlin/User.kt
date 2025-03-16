package pt.isel.pipeline

data class User (
   val id: Long,
   val email: String,
   val password: String,
   val role: UserRole
)