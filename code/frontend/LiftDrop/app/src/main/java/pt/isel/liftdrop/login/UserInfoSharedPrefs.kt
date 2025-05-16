package pt.isel.liftdrop.login

import android.content.Context
import pt.isel.liftdrop.login.model.UserInfo
import pt.isel.liftdrop.login.model.UserInfoRepository
import androidx.core.content.edit

/**
 * A user information repository implementation supported in shared preferences
 */
class UserInfoSharedPrefs(private val context: Context): UserInfoRepository {

    private val userUsernameKey = "Username"
    private val userBearerKey = "Bearer"
    private val userCourierIdKey = "CourierId"

    private val prefs by lazy {
        context.getSharedPreferences("UserInfoPrefs", Context.MODE_PRIVATE)
    }

    override var userInfo: UserInfo?
        get() {
            val savedUsername = prefs.getString(userUsernameKey, null)
            val savedBearer = prefs.getString(userBearerKey,null)
            val savedCourierId = prefs.getString(userCourierIdKey, null)

            return if (savedUsername != null && savedBearer != null)
                UserInfo(savedUsername, savedBearer, savedCourierId ?: "")
            else
                null
        }

        set(value) {
            if (value == null)
                prefs.edit {
                    remove(userUsernameKey)
                        .remove(userBearerKey)
                        .remove(userCourierIdKey)
                }
            else
                prefs.edit {
                    putString(userUsernameKey, value.username)
                        .putString(userBearerKey, value.bearer)
                        .putString(userCourierIdKey, value.courierId)
                }
        }
}