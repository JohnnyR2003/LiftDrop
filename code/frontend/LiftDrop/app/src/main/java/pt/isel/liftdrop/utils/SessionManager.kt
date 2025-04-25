package pt.isel.liftdrop.utils

import android.content.Context
import androidx.core.content.edit

object SessionManager {

    private const val PREFS_NAME = "user_session"
    private const val IS_LOGGED_IN_KEY = "is_logged_in"

    fun isUserLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(IS_LOGGED_IN_KEY, false)
    }

    fun setUserLoggedIn(context: Context, isLoggedIn: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit() { putBoolean(IS_LOGGED_IN_KEY, isLoggedIn) }
    }

    fun clearSession(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit() { clear() }
    }
}