package pt.isel.pipeline.pt.isel.liftdrop

import pt.isel.liftdrop.RequestStatus

data class Request(
    val id: Long,
    val clientId: Long,
    val courierId: Long?,
    val requestStatus: RequestStatus,
    val createdAt: Long,
    val details: RequestDetails,
)
