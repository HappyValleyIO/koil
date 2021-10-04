package org.koil.user.password

import org.koil.notifications.EmailNotificationService
import org.koil.user.AccountRepository
import org.springframework.stereotype.Service
import java.util.*

interface PasswordService {
    fun requestPasswordReset(email: String): PasswordResetRequestResult
    fun resetPassword(code: UUID, email: String, password: HashedPassword): PasswordResetResult
}

@Service
class DefaultPasswordService(
    private val notifications: EmailNotificationService,
    private val accountRepository: AccountRepository
) : PasswordService {
    override fun requestPasswordReset(email: String): PasswordResetRequestResult {
        // Create the unique password reset code
        return accountRepository.findAccountByEmailAddressIgnoreCase(email)?.let {
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

    override fun resetPassword(code: UUID, email: String, password: HashedPassword): PasswordResetResult {
        return accountRepository.findAccountByPasswordResetCode(code)?.let {
            try {
                val updated = it.updatePassword(password)
                accountRepository.save(updated)
                PasswordResetResult.Success
            } catch (e: Throwable) {
                PasswordResetResult.FailedUnexpectedly
            }
        } ?: PasswordResetResult.InvalidCredentials
    }
}
