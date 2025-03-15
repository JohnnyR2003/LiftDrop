package pt.isel.pipeline

sealed class User {
    abstract val id: Long
    abstract val email: String
    abstract val password: String
}