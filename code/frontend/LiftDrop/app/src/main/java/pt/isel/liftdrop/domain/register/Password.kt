package pt.isel.liftdrop.domain.register

private const val MIN_PASSWORD_LENGTH = 8
private const val MAX_PASSWORD_LENGTH = 40

data class Password(val value: String) {

    init {
        require(isValid(value)) { "Invalid password" }
    }

    companion object {
        fun isValid(value: String): Boolean {
            if (value.isEmpty()) return false
            if (value.isBlank()) return false
            if (value.length !in MIN_PASSWORD_LENGTH - 1..<MAX_PASSWORD_LENGTH) return false
            return true
        }
    }
}
