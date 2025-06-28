package pt.isel.liftdrop.home.model

data class ResultMessage(
    val type: String,
    val subType: String?,
    val message: String,
    val detail: String? = null,
)
