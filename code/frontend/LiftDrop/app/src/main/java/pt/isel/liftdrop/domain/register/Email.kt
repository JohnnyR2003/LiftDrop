package pt.isel.liftdrop.domain.register

private const val REGEX_PATTERN = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\$"

data class Email(val value: String) {

    init {
        require(isValid(value)) { "Invalid email address" }
    }

    companion object  {
         fun isValid(value: String): Boolean = value.matches(Regex(REGEX_PATTERN))
    }
}