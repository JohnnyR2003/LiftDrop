package pt.isel.liftdrop.model

data class AcceptRequestOutputModel(
    val type: String = "RESPONSE",
    val status: String = "ACCEPT",
    val requestId: Int,
)
