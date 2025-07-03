package pt.isel.liftdrop.home.model

enum class MessageType(val value: String) {
    DELIVERY_REQUEST("DELIVERY_REQUEST"),
    DELIVERY_UPDATE("DELIVERY_UPDATE"),
    SUCCESS("SUCCESS"),
    ERROR("ERROR");

    companion object {
        fun fromValue(value: String): MessageType? =
            MessageType.entries.find { it.value == value }
    }
}