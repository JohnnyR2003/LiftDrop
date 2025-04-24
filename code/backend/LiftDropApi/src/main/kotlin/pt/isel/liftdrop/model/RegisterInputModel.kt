package pt.isel.liftdrop.model

data class RegisterInputModel (
    val name: String,
    val email: String,
    val password: String,
    val address: AddressInputModel,
)