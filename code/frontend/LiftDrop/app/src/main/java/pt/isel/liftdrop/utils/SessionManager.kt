import android.content.Context

object SessionManager {
    private const val PREF_NAME = "LiftDropSession"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_TOKEN = "user_token"

    fun setUserLoggedIn(context: Context, isLoggedIn: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isUserLoggedIn(context: Context): Boolean = getUserToken(context) != null


    fun getUserToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_TOKEN, null)
    }

    fun setUserToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_USER_TOKEN, token).apply()
    }

    fun clearSession(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
