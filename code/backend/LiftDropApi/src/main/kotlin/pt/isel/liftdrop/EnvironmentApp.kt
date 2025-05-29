package pt.isel.liftdrop

object EnvironmentApp {
    fun getDbUrl(): String {
        return System.getenv("DB_URL") ?: return "jdbc:postgresql://localhost:5432/liftdrop"
    }

    fun getDbUser(): String {
        return  System.getenv("DB_USER") ?: return "postgres"
    }

    fun getDbPassword(): String {
        return System.getenv("DB_PASS") ?: return "postgres"
    }

//    postgresql://liftdrop_ceib_user:NjEbzbVyGBsvF0AoG7c8C4VOp5u3OblK@dpg-d0s9dkjuibrs738179c0-a/liftdrop_ceib

    fun getGoogleAPIKey(): String =
        System.getenv("GOOGLE_API_KEY")
            ?: throw IllegalStateException("GOOGLE_API_KEY environment variable not set")
}
