package pt.isel.liftdrop

object EnvironmentApp {
    fun getDbUrl(): String = "jdbc:postgresql://localhost:5432/liftdrop"

    fun getDbUser(): String = "postgres"

    fun getDbPassword(): String = "postgres"

    fun getGoogleAPIKey(): String =
        System.getenv("GOOGLE_API_KEY")
            ?: throw IllegalStateException("GOOGLE_API_KEY environment variable not set")
}
