package pt.isel.liftdrop.model

data class DeclineRequestOutputModel(
    val type: String = "RESPONSE",
    val status: String = "DECLINE",
    val requestId: Int,
)
