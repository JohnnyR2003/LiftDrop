package pt.isel.liftdrop.login.model

interface PreferencesRepository {

    suspend fun isLoggedIn(): Boolean

    /**
     * Gets the user info if it exists, null otherwise.
     */
    suspend fun getUserInfo(): UserInfo?

    /**
     * Updates the user info.
     */
    suspend fun setUserInfo(userInfo: UserInfo)

    /**
     * Clears the user info.
     */
    suspend fun clearUserInfo(userInfo: UserInfo)
}