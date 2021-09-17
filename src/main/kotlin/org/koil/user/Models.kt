package org.koil.user

import org.koil.auth.AuthAuthority
import org.springframework.context.ApplicationEvent
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table
import org.springframework.security.core.userdetails.UserDetails
import java.time.Duration
import java.time.Instant
import java.util.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

data class AccountVerification(
    val verificationCode: UUID,
    val createdAt: Instant,
    val expiresAt: Instant
)

data class AccountPasswordReset(
    val resetCode: UUID,
    val createdAt: Instant = Instant.now(),
    val expiresAt: Instant = Instant.now().plus(Duration.ofHours(3))
)

data class UpdateUserSettingsRequest(
    @get:NotEmpty(message = "Name cannot be empty") val name: String,
    @get:Email(message = "Must be a valid email address") val email: String,
    val weeklySummary: Boolean?,
    val updateOnAccountChange: Boolean?
) {
    val normalizedEmail: String = email.trim().toLowerCase()

    val notificationSettings: NotificationSettings = NotificationSettings(
        weeklyActivity = weeklySummary ?: false,
        emailOnAccountChange = updateOnAccountChange ?: false
    )

    fun update(account: Account): Account =
        account.updateNotificationSettings(this.notificationSettings)
            .updateName(this.name)
            .updateEmail(this.normalizedEmail)
}

@Table("account_authorities")
data class AccountAuthority(val authority: AuthAuthority, val authorityGranted: Instant = Instant.now())

@Table("accounts")
data class Account(
    @Id val accountId: Long?,
    val startDate: Instant,
    val fullName: String,
    val handle: String,
    val publicAccountId: UUID,
    val emailAddress: String,
    val password: String,
    val stopDate: Instant?,
    @Embedded.Empty val notificationSettings: NotificationSettings,
    @MappedCollection(idColumn = "account_id") val authorities: List<AccountAuthority>,
    @MappedCollection(idColumn = "account_id") val accountVerification: AccountVerification? = null,
    @MappedCollection(idColumn = "account_id") val accountPasswordReset: AccountPasswordReset? = null
) {
    fun isAdmin(): Boolean = authorities.map { it.authority }.contains(AuthAuthority.ADMIN)

    fun withPasswordReset(code: UUID): Account =
        this.copy(accountPasswordReset = AccountPasswordReset(code))

    fun updatePassword(encodedPassword: String): Account =
        this.copy(password = encodedPassword)

    fun updateName(name: String): Account =
        this.copy(fullName = name)

    fun updateEmail(email: String): Account =
        this.copy(emailAddress = email)

    fun updateNotificationSettings(notificationSettings: NotificationSettings): Account =
        this.copy(notificationSettings = notificationSettings)
}

data class AccountCreationEvent(val src: Any, val account: Account) : ApplicationEvent(src)

data class EnrichedUserDetails(val details: UserDetails, val accountId: Long, val handle: String) :
    UserDetails by details {
    fun isAdmin(): Boolean {
        return authorities.contains(AuthAuthority.ADMIN.grantedAuthority)
    }
}

data class NotificationSettings(
    val weeklyActivity: Boolean,
    val emailOnAccountChange: Boolean
) {
    companion object {
        val default: NotificationSettings =
            NotificationSettings(weeklyActivity = false, emailOnAccountChange = true)

    }
}

data class NoAccountFoundUnexpectedlyException(val accountId: Long) :
    RuntimeException("Could not find account with ID $accountId")

sealed class AccountUpdateResult {
    data class AccountUpdated(val account: Account) : AccountUpdateResult()
    data class EmailAlreadyInUse(val email: String) : AccountUpdateResult()
}
