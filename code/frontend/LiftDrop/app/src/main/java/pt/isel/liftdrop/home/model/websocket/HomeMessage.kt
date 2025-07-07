package pt.isel.liftdrop.home.model.websocket

import kotlinx.serialization.Serializable

sealed interface HomeMessage

@Serializable
data class IncomingRequestDetails(
    val type: String = "DELIVERY_REQUEST",
    val requestId: String,
    val courierId : String,
    val pickupLatitude: Double,
    val pickupLongitude: Double,
    val pickupAddress: String,
    val dropoffLatitude: Double,
    val dropoffLongitude: Double,
    val dropoffAddress: String,
    val item: String,
    val quantity: Int,
    val deliveryEarnings: String,
    val deliveryKind: String
): HomeMessage
data class DeliveryUpdate(
    val type : String = "DELIVERY_UPDATE",
    val hasBeenAccepted: Boolean,
    val hasBeenPickedUp: Boolean,
    val pinCode: String?,
) : HomeMessage
data class ResultMessage(
    val type: ResultType,
    val subType: ResultSubType = ResultSubType.UNKNOWN,
    val message: String,
    val detail: String? = null
) : HomeMessage