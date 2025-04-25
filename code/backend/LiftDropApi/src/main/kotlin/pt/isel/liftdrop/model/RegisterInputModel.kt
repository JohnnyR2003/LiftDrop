package pt.isel.liftdrop.model

data class RegisterClientInputModel(
    val name: String,
    val email: String,
    val password: String,
    val address: AddressInputModel,
)

data class RegisterCourierInputModel(
    val email: String,
    val password: String,
    val name: String,
)
