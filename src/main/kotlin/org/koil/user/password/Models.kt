package org.koil.user.password

import org.springframework.data.relational.core.mapping.Column
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Duration
import java.time.Instant
import java.util.*

data class AccountPasswordReset(
    val resetCode: UUID,
    val createdAt: Instant = Instant.now(),
    val expiresAt: Instant = Instant.now().plus(Duration.ofHours(3))
)

data class HashedPassword(@Column("password") val encodedPassword: String) {
    init {
        require(encodedPassword.startsWith("{")) {
            "Password must be encoded to be stored in a HashedPassword object."
        }
    }

    companion object {
        private val encoder: PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()

        fun encode(password: String): HashedPassword =
            HashedPassword(encoder.encode(password))
    }
}

sealed class PasswordResetRequestResult {
    object Success : PasswordResetRequestResult()
    object FailedUnexpectedly : PasswordResetRequestResult()
    object CouldNotFindUserWithEmail : PasswordResetRequestResult()
}

sealed class PasswordResetResult {
    object Success : PasswordResetResult()
    object InvalidCredentials : PasswordResetResult()
    object FailedUnexpectedly : PasswordResetResult()
}
