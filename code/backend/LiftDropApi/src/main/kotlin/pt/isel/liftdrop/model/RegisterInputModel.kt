package pt.isel.liftdrop.model

import pt.isel.liftdrop.Location

data class RegisterClientInputModel(
    val name: String,
    val email: String,
    val password: String,
    val address: AddressInputModel,
)

data class RegisterCourierInputModel(
    val name: String,
    val email: String,
    val password: String,
    val location: Location,
)
