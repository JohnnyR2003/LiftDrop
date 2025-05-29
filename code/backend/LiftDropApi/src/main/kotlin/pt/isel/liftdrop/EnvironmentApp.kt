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

//    postgresql://liftdrop_ceib_user:NjEbzbVyGBsvF0AoG7c8C4VOp5u3OblK@dpg-d0s9dkjuibrs738179c0-a/liftdrop_ceib

    fun getGoogleAPIKey(): String =
        System.getenv("GOOGLE_API_KEY")
            ?: throw IllegalStateException("GOOGLE_API_KEY environment variable not set")
}
