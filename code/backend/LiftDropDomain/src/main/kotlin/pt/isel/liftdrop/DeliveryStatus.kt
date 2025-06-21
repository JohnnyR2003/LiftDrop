package pt.isel.liftdrop

enum class DeliveryStatus {
    HEADING_TO_PICKUP,
    HEADING_TO_DROPOFF,
    ;

    companion object {
        fun fromString(status: String): DeliveryStatus =
            entries.find { it.name.equals(status, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown delivery status: $status")
    }

    override fun toString(): String = this.name

    fun toDeliveryKind(): DeliveryKind =
        when (this) {
            HEADING_TO_PICKUP -> DeliveryKind.DEFAULT
            HEADING_TO_DROPOFF -> DeliveryKind.RELAY
        }
}
