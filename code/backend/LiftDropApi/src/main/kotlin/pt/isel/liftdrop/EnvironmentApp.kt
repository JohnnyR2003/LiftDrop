package pt.isel.liftdrop

object EnvironmentApp {
    fun getDbUrl(): String {
        val dbUrl = System.getenv("DB_URL") ?: return "jdbc:postgresql://localhost:5432/liftdrop"
        return "jdbc:${dbUrl}"
    }

    fun getDbUser(): String {
        val dbUrl = System.getenv("DB_URL") ?: return "postgres"
        return dbUrl.split("://")[1].split(":")[0]
    }

    fun getDbPassword(): String {
        val dbUrl = System.getenv("DB_URL") ?: return "postgres"
        return dbUrl.split(":")[2].split("@")[0]
    }

    fun getGoogleAPIKey(): String =
        System.getenv("GOOGLE_API_KEY")
            ?: throw IllegalStateException("GOOGLE_API_KEY environment variable not set")
}
