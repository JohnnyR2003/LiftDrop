package pt.isel.pipeline.pt.isel.liftdrop

import pt.isel.liftdrop.Status

data class RequestDTO(
    val id: Int,
    val clientId: Int,
    val courierId: Int?,
    val requestStatus: Status,
)
