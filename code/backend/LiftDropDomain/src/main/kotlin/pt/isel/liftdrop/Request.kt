package pt.isel.liftdrop

import kotlinx.datetime.Instant
import pt.isel.pipeline.pt.isel.liftdrop.RequestDetails

data class Request(
    val id: Int,
    val clientId: Int,
    val courierId: Int?,
    val requestStatus: RequestStatus,
    val createdAt: Instant,
    val details: RequestDetails,
)
