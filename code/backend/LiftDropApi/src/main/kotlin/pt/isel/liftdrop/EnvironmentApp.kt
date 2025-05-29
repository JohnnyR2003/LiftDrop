package pt.isel.liftdrop

object EnvironmentApp {
    fun getDbUrl(): String {
        val dbUrl = System.getenv("DB_URL")
        return if (dbUrl == null) {
            "jdbc:postgresql://localhost:5432/liftdrop"
        } else {
            "jdbc:$dbUrl"
        }
    }

    fun getDbUser(): String {
        val dbUrl = System.getenv("DB_URL") ?: return "postgres"
        val regex = Regex(".*://(.*?):.*?@")
        return regex.find(dbUrl)?.groupValues?.get(1) ?: "postgres"
    }

    fun getDbPassword(): String {
        val dbUrl = System.getenv("DB_URL") ?: return "postgres"
        val regex = Regex(".*://.*?:(.*?)@")
        return regex.find(dbUrl)?.groupValues?.get(1) ?: "postgres"
    }

    // jdbc:postgresql://localhost:5432/liftdrop?user=postgres&password=postgres

    fun getGoogleAPIKey(): String =
        System.getenv("GOOGLE_API_KEY")
            ?: throw IllegalStateException("GOOGLE_API_KEY environment variable not set")
}
//