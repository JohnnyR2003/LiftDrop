package pt.isel.services.utils

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

object Codify {
    private val passwordEncoder = BCryptPasswordEncoder()

    fun String.codifyPassword(): String {
        return passwordEncoder.encode(this)
    }

    fun matchesPassword(rawPassword: String, encodedPassword: String): Boolean {
        return passwordEncoder.matches(rawPassword, encodedPassword)
    }
}