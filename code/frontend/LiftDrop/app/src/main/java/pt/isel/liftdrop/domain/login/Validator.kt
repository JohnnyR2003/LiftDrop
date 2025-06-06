package pt.isel.liftdrop.domain.login

import pt.isel.liftdrop.domain.register.Email
import pt.isel.liftdrop.domain.register.Password

object Validator {
    fun areRegistrationCredentialsValid(
        username: String,
        email: String,
        password: String,
    ): Boolean = Email.isValid(email) && Password.isValid(password)

    fun areLoginCredentialsValid(
        email: String,
        password: String,
    ): Boolean = Email.isValid(email) && Password.isValid(password)
}