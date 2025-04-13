package pt.isel.pipeline.pt.isel.liftdrop

import pt.isel.liftdrop.RequestStatus

data class Request(
    val id: Int,
    val clientId: Int,
    val courierId: Int?,
    val requestStatus: RequestStatus,
    val createdAt: Int,
    val details: RequestDetails,
)
