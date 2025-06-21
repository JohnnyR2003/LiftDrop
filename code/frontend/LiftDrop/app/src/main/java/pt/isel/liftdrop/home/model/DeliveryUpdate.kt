package pt.isel.liftdrop.home.model

data class DeliveryUpdate(
    val type : String = "DELIVERY_UPDATE",
    val hasBeenAccepted: Boolean,
    val hasBeenPickedUp: Boolean,
    val pinCode: String?,
)
