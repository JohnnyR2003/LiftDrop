package pt.isel.liftdrop.services.http

data class Problem(
    val type: String?,
    val title: String?,
    val status: Int?,
    val detail: String?,
    val instance: String? = null
)
