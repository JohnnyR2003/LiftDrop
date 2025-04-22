package pt.isel.services.utils

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

object Codify {
    private val passwordEncoder = BCryptPasswordEncoder()

    fun String.encodePassword(): String = passwordEncoder.encode(this)

    fun matchesPassword(
        rawPassword: String,
        encodedPassword: String,
    ): Boolean = passwordEncoder.matches(rawPassword, encodedPassword)
}
