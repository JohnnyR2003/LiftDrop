package pt.isel.liftdrop

data class Address(
    val country: String,
    val city: String,
    val street: String,
    val streetNumber: String?,
    val floor: String?,
    val zipCode: String,
)
