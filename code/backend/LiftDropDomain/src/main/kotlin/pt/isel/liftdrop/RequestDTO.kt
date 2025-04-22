package pt.isel.liftdrop

data class RequestDTO(
    val id: Int,
    val clientId: Int,
    val courierId: Int?,
    val requestStatus: Status,
)
