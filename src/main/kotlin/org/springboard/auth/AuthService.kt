package org.springboard.auth

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.mapTo
import org.springboard.notifications.NotificationService
import org.springboard.user.Account
import org.springboard.user.UserPersistence
import org.springboard.user.UserQueryResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
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

interface AuthPersistence {
    fun persistPasswordResetCode(account: Long, code: UUID?, expires: ZonedDateTime)
    fun getUserByCode(code: UUID): Account?
    fun updatePassword(accountId: Long, password: String)
}

@Component
class AuthServiceImpl(
        private val users: UserPersistence,
        private val persistence: AuthPersistence,
        private val notifications: NotificationService,
        private val passwordEncoder: PasswordEncoder
) : AuthService {
    override fun requestPasswordReset(email: String): PasswordResetRequestResult {
        // Create the unique password reset code
        return users.getUserByEmail(email)?.let {
            try {
                val code = UUID.randomUUID()
                persistence.persistPasswordResetCode(it.accountId, code, ZonedDateTime.now().plusHours(3))
                notifications.sendPasswordResetEmail(email, code)
                PasswordResetRequestResult.Success
            } catch (t: Throwable) {
                PasswordResetRequestResult.FailedUnexpectedly
            }
        } ?: PasswordResetRequestResult.CouldNotFindUserWithEmail
    }

    override fun resetPassword(code: UUID, email: String, password: String): PasswordResetResult {
        return persistence.getUserByCode(code)?.let {
            try {
                persistence.updatePassword(it.accountId, passwordEncoder.encode(password))
                PasswordResetResult.Success
            } catch (e: Throwable) {
                PasswordResetResult.FailedUnexpectedly
            }
        } ?: PasswordResetResult.InvalidCredentials
    }
}

@Component
class AuthPersistenceImpl(@Autowired private val jdbi: Jdbi) : AuthPersistence {

    override fun persistPasswordResetCode(account: Long, code: UUID?, expires: ZonedDateTime) {
        return jdbi.useHandle<RuntimeException> {
            it.createUpdate(
                    """
                INSERT INTO account_password_reset
                    (account_id, reset_code, expires_at)
                VALUES
                    (:acct, :code, :expires)
            """.trimIndent()
            )
                    .bind("acct", account)
                    .bind("code", code)
                    .bind("expires", expires)
                    .execute()
        }
    }

    override fun getUserByCode(code: UUID): Account? {
        return jdbi.withHandle<Account?, RuntimeException> {
            it.createQuery(
                    """
                SELECT a.*, ac.email_address, ac.password FROM account_password_reset apr
                    JOIN accounts a ON apr.account_id = a.account_id
                    JOIN account_credentials ac ON apr.account_id = ac.account_id
                    WHERE apr.reset_code = :code
                    AND apr.expires_at > NOW()
            """.trimIndent()
            )
                    .bind("code", code)
                    .mapTo<UserQueryResult>()
                    .findFirst()
                    .map { Account(it.accountId, it.email, it.handle) }
                    .orElse(null)
        }
    }

    override fun updatePassword(accountId: Long, password: String) {
        jdbi.useHandle<RuntimeException> {

            it.createUpdate(
                    """
                UPDATE account_credentials
                SET password = :password
                WHERE account_id = :account
            """.trimIndent()
            )
                    .bind("password", password)
                    .bind("account", accountId)
                    .execute()

            it.createUpdate(
                    """
                DELETE FROM account_password_reset
                WHERE account_id = :acct
            """.trimIndent()
            )
                    .bind("acct", accountId)
                    .execute()
        }
    }
}
