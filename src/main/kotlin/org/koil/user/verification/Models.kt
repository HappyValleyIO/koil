package org.koil.user.verification

import dev.forkhandles.result4k.Result
import org.koil.extensions.toFailure
import org.koil.extensions.toSuccess
import org.koil.user.Account
import org.springframework.context.ApplicationEvent
import org.springframework.data.annotation.Transient
import java.time.Instant
import java.util.*

data class AccountVerification(
    val verificationCode: UUID,
    val verificationRequestedAt: Instant,
    val verifiedAt: Instant?
) {
    companion object {
        fun create(): AccountVerification =
            AccountVerification(UUID.randomUUID(), verificationRequestedAt = Instant.now(), verifiedAt = null)
    }

    @Transient
    val isVerified: Boolean = verifiedAt != null

    fun verify(code: UUID): Result<AccountVerification, AccountVerificationViolations> =
        when {
            isVerified -> AccountVerificationViolations.AccountAlreadyVerified.toFailure()
            code != verificationCode -> AccountVerificationViolations.IncorrectCode.toFailure()
            else -> this.copy(verifiedAt = Instant.now()).toSuccess()
        }
}

sealed class AccountVerificationViolations {
    object IncorrectCode : AccountVerificationViolations()
    object AccountAlreadyVerified : AccountVerificationViolations()
}

data class AccountVerifiedEvent(val account: Account, private val src: Any) : ApplicationEvent(src)
