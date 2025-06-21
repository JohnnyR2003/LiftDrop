package pt.isel.liftdrop

enum class DeliveryKind {
    DEFAULT,
    RELAY,
    ;

    companion object {
        fun fromString(kind: String): DeliveryKind =
            entries.find { it.name.equals(kind, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown delivery kind: $kind")
    }

    override fun toString(): String = this.name
}
