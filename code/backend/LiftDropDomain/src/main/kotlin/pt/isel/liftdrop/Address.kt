package pt.isel.liftdrop

data class Address(
    val id: Int,
    val country: String,
    val city: String,
    val street: String,
    val streetNumber: String?,
    val floor: String?,
    val zipCode: String,
)
