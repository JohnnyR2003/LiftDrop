package pt.isel.liftdrop

data class DeliveryUpdateMessage(
    val type: String = "DELIVERY_UPDATE",
    val hasBeenAccepted: Boolean,
    val pinCode: String,
)
