package pt.isel.liftdrop.model

data class AddressInputModel(
    val street: String,
    val city: String,
    val country: String,
    val zipcode: String,
    val streetNumber: String?,
    val floor: String?
)