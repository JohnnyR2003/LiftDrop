import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import pt.isel.liftdrop.login.model.UserInfo
import pt.isel.liftdrop.login.model.UserInfoRepository

class UserInfoSharedPrefs(private val context: Context) : UserInfoRepository {

    companion object {
        private const val PREFS_NAME = "UserInfoPrefs"
        private const val KEY_USERNAME = "Username"
        private const val KEY_BEARER = "Bearer"
        private const val KEY_COURIER_ID = "CourierId"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _userInfoFlow = MutableStateFlow(readUserInfoFromPrefs())
    override val userInfoFlow: StateFlow<UserInfo?> get() = _userInfoFlow

    private fun readUserInfoFromPrefs(): UserInfo? {
        val username = prefs.getString(KEY_USERNAME, null)
        val bearer = prefs.getString(KEY_BEARER, null)
        val courierId = prefs.getString(KEY_COURIER_ID, null)
        return if (username != null && bearer != null)
            UserInfo(username, bearer, courierId.toString())
        else
            null
    }

    override var userInfo: UserInfo?
        get() = _userInfoFlow.value
        set(value) {
            with(prefs.edit()) {
                if (value == null) {
                    remove(KEY_USERNAME)
                    remove(KEY_BEARER)
                    remove(KEY_COURIER_ID)
                } else {
                    putString(KEY_USERNAME, value.username)
                    putString(KEY_BEARER, value.bearer)
                    putString(KEY_COURIER_ID, value.courierId)
                }
                apply()
            }
            _userInfoFlow.value = value
        }
}
