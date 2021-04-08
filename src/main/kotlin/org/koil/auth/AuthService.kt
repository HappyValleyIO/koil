package org.koil.auth

import org.koil.notifications.NotificationService
import org.koil.user.AccountRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.util.*

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

interface AuthService {
    fun requestPasswordReset(email: String): PasswordResetRequestResult
    fun resetPassword(code: UUID, email: String, password: String): PasswordResetResult
}

@Component
class AuthServiceImpl(
    private val notifications: NotificationService,
    private val passwordEncoder: PasswordEncoder,
    private val accountRepository: AccountRepository
) : AuthService {
    override fun requestPasswordReset(email: String): PasswordResetRequestResult {
        // Create the unique password reset code
        return accountRepository.findAccountByEmailAddress(email)?.let {
            try {
                val code = UUID.randomUUID()
                accountRepository.save(it.withPasswordReset(code))
                notifications.sendPasswordResetEmail(email, code)
                PasswordResetRequestResult.Success
            } catch (t: Throwable) {
                PasswordResetRequestResult.FailedUnexpectedly
            }
        } ?: PasswordResetRequestResult.CouldNotFindUserWithEmail
    }

    override fun resetPassword(code: UUID, email: String, password: String): PasswordResetResult {
        return accountRepository.findAccountByPasswordResetCode(code)?.let {
            try {
                val updated = it.updatePassword(passwordEncoder.encode(password))
                accountRepository.save(updated)
                PasswordResetResult.Success
            } catch (e: Throwable) {
                PasswordResetResult.FailedUnexpectedly
            }
        } ?: PasswordResetResult.InvalidCredentials
    }
}
