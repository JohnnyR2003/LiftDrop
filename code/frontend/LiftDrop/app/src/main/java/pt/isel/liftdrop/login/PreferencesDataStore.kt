package pt.isel.liftdrop.login

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import pt.isel.liftdrop.login.model.PreferencesRepository
import pt.isel.liftdrop.login.model.UserInfo


class PreferencesDataStore(
    private val store: DataStore<Preferences>
) : PreferencesRepository {

    companion object {
        private const val USER_KEY = "user"
        private const val USER_NAME_KEY = "$USER_KEY-name"
        private const val USER_ID_KEY = "$USER_KEY-id"
        private const val USER_TOKEN_KEY = "$USER_KEY-token"
        private const val USER_EMAIL_KEY = "$USER_KEY-email"
    }

    private val nameKey = stringPreferencesKey(USER_NAME_KEY)
    private val idKey = intPreferencesKey(USER_ID_KEY)
    private val tokenKey = stringPreferencesKey(USER_TOKEN_KEY)
    private val emailKey = stringPreferencesKey(USER_EMAIL_KEY)

    override suspend fun isLoggedIn() = store.data.first()[tokenKey] != null

    override suspend fun getUserInfo(): UserInfo? {
        val preferences = store.data.first()
        val username = preferences[nameKey]
        val courierId = preferences[idKey]
        val token = preferences[tokenKey]
        val email = preferences[emailKey]
        Log.v(
            "PreferencesDataStore",
            "username: $username, id: $courierId, token: $token, email: $email"
        )
        return if (username != null && courierId != null && token != null && email != null
        ) {
            UserInfo(
                id = courierId,
                username = username,
                email = email,
                bearer = token,
            )
        } else {
            null
        }
    }

    override suspend fun setUserInfo(userInfo: UserInfo) {
        store.edit { preferences ->
            preferences[nameKey] = userInfo.username
            preferences[idKey] = userInfo.id
            preferences[tokenKey] = userInfo.bearer
            preferences[emailKey] = userInfo.email
        }
    }

    override suspend fun clearUserInfo(userInfo: UserInfo) {
        if (userInfo == getUserInfo()) {
            store.edit { preferences ->
                preferences.remove(nameKey)
                preferences.remove(idKey)
                preferences.remove(tokenKey)
                preferences.remove(emailKey)
            }
        }
    }
}