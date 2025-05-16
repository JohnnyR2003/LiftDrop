package pt.isel.liftdrop.login.model

data class UserInfo(val username: String, val bearer: String, val courierId: String) {
    init {
        require(validateUserInfoParts(username, bearer, courierId))
    }
}

fun userInfoOrNull(username: String, bearer: String, courierId: String): UserInfo? =
    if (validateUserInfoParts(username, bearer, courierId))
        UserInfo(username, bearer, courierId)
    else
        null

fun validateUserInfoParts(username: String, bearer: String, courierId: String) =
    (username.isNotBlank() && bearer.isNotBlank() && bearer != "null")