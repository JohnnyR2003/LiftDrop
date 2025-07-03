package pt.isel.liftdrop.home.model

enum class DeliveryStatus {
    HEADING_TO_PICKUP,
    HEADING_TO_DROPOFF;

    companion object {
        fun fromString(status: String): DeliveryStatus =
            DeliveryStatus.entries.find { it.name == status }
                ?: throw IllegalArgumentException("Unknown delivery status: $status")
    }
}