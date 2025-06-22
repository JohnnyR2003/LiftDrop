package pt.isel.liftdrop.model

import pt.isel.liftdrop.Address

data class AddressInputModel(
    val street: String,
    val city: String,
    val country: String,
    val zipcode: String,
    val streetNumber: String?,
    val floor: String?,
)

fun AddressInputModel?.toAddress(): Address? =
    if (this == null) {
        null
    } else {
        Address(
            street = street,
            city = city,
            country = country,
            zipCode = zipcode,
            streetNumber = streetNumber,
            floor = floor,
        )
    }
