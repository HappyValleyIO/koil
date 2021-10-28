package org.koil.user

import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.map
import org.koil.auth.UserAuthority
import org.koil.user.password.AccountPasswordReset
import org.koil.user.password.HashedPassword
import org.koil.user.verification.AccountVerification
import org.koil.user.verification.AccountVerificationViolations
import org.springframework.context.ApplicationEvent
import org.springframework.data.annotation.Id
import org.springframework.data.domain.AbstractAggregateRoot
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("account_authorities")
data class AccountAuthority(val authority: UserAuthority, val authorityGranted: Instant)

@Table("accounts")
data class Account(
    @Id val accountId: Long?,
    val companyId: Long,
    val startDate: Instant,
    val fullName: String,
    val handle: String,
    val publicAccountId: UUID,
    val emailAddress: String,
    @Embedded.Empty val password: HashedPassword,
    @Embedded.Empty val notificationSettings: NotificationSettings,
    @Embedded.Empty val accountVerification: AccountVerification,
    @MappedCollection(idColumn = "account_id") val authorities: List<AccountAuthority>,
    @MappedCollection(idColumn = "account_id") val accountPasswordReset: AccountPasswordReset? = null,
    val stopDate: Instant? = null
) : AbstractAggregateRoot<Account>() {
    companion object {
        fun create(
            companyId: Long,
            fullName: String,
            handle: String,
            emailAddress: String,
            password: HashedPassword,
            authorities: List<UserAuthority>,
        ): Account = Account(
            accountId = null,
            companyId = companyId,
            startDate = Instant.now(),
            fullName = fullName,
            handle = handle,
            publicAccountId = UUID.randomUUID(),
            emailAddress = emailAddress,
            password = password,
            stopDate = null,
            notificationSettings = NotificationSettings.default,
            authorities = listOf(),
            accountVerification = AccountVerification.create()
        ).withAuthorities(authorities)
    }

    init {
        if (accountId == null) {
            registerEvent(AccountCreationEvent(this, this))
        }
    }

    fun isVerified(): Boolean =
        accountVerification.isVerified

    fun verifyAccount(code: UUID): Result<Account, AccountVerificationViolations> {
        return accountVerification.verify(code)
            .map {
                this.copy(accountVerification = it).andEventsFrom(this)
            }
    }

    fun isCompanyOwner(): Boolean = authorities.map { it.authority }.contains(UserAuthority.COMPANY_OWNER)

    fun isAdmin(): Boolean = authorities.map { it.authority }.contains(UserAuthority.ADMIN)

    fun withPasswordReset(code: UUID): Account =
        this.copy(accountPasswordReset = AccountPasswordReset(code)).andEventsFrom(this)

    fun updatePassword(password: HashedPassword): Account =
        this.copy(password = password).andEventsFrom(this)

    fun updateName(name: String): Account =
        this.copy(fullName = name).andEventsFrom(this)

    fun updateEmail(email: String): Account {
        return if (email != this.emailAddress) {
            this.copy(emailAddress = email, accountVerification = AccountVerification.create())
                .andEventsFrom(this)
                .also {
                    it.registerEvent(EmailUpdatedEvent(this, it))
                }
        } else {
            this
        }
    }

    fun updateHandle(handle: String): Account =
        this.copy(handle = handle).andEventsFrom(this)


    fun updateNotificationSettings(notificationSettings: NotificationSettings): Account =
        this.copy(notificationSettings = notificationSettings).andEventsFrom(this)

    fun grantAuthority(authority: UserAuthority): Account =
        if (this.authorities.map { it.authority }.contains(authority)) {
            this
        } else {
            this.copy(authorities = this.authorities + AccountAuthority(authority, Instant.now()))
                .andEventsFrom(this)
        }

    /**
     * Grants a user only the authorities in the list provided. Any existing authorities not in the list are revoked.
     */
    fun withAuthorities(newAuthorities: List<UserAuthority>): Account {
        val updated = this.copy(authorities = this.authorities.filter { newAuthorities.contains(it.authority) })
            .andEventsFrom(this)

        return newAuthorities.fold(updated) { account, authority ->
            account.grantAuthority(authority)
        }
    }
}

data class AccountCreationEvent(val src: Any, val account: Account) : ApplicationEvent(src)
data class EmailUpdatedEvent(val src: Any, val account: Account) : ApplicationEvent(src)

data class NotificationSettings(
    val weeklyActivity: Boolean,
    val emailOnAccountChange: Boolean
) {
    companion object {
        val default: NotificationSettings =
            NotificationSettings(weeklyActivity = false, emailOnAccountChange = true)
    }
}
