package org.koil.user

import org.koil.auth.UserAuthority
import org.springframework.context.ApplicationEvent
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Embedded
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table
import org.springframework.security.crypto.factory.PasswordEncoderFactories.createDelegatingPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.Duration
import java.time.Instant
import java.util.*

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

data class HashedPassword(@Column("password") val encodedPassword: String) {
    init {
        require(encodedPassword.startsWith("{")) {
            "Password must be encoded to be stored in a HashedPassword object."
        }
    }

    companion object {
        private val encoder: PasswordEncoder = createDelegatingPasswordEncoder()

        fun encode(password: String): HashedPassword =
            HashedPassword(encoder.encode(password))
    }
}


@Table("account_authorities")
data class AccountAuthority(val authority: UserAuthority, val authorityGranted: Instant)

@Table("accounts")
data class Account(
    @Id val accountId: Long?,
    val startDate: Instant,
    val fullName: String,
    val handle: String,
    val publicAccountId: UUID,
    val emailAddress: String,
    @Embedded.Empty val password: HashedPassword,
    val stopDate: Instant?,
    @Embedded.Empty val notificationSettings: NotificationSettings,
    @MappedCollection(idColumn = "account_id") val authorities: List<AccountAuthority>,
    @MappedCollection(idColumn = "account_id") val accountVerification: AccountVerification? = null,
    @MappedCollection(idColumn = "account_id") val accountPasswordReset: AccountPasswordReset? = null
) {
    fun isAdmin(): Boolean = authorities.map { it.authority }.contains(UserAuthority.ADMIN)

    fun withPasswordReset(code: UUID): Account =
        this.copy(accountPasswordReset = AccountPasswordReset(code))

    fun updatePassword(password: HashedPassword): Account =
        this.copy(password = password)

    fun updateName(name: String): Account =
        this.copy(fullName = name)

    fun updateEmail(email: String): Account =
        this.copy(emailAddress = email)

    fun updateHandle(handle: String): Account =
        this.copy(handle = handle)

    fun updateNotificationSettings(notificationSettings: NotificationSettings): Account =
        this.copy(notificationSettings = notificationSettings)

    fun grantAuthority(authority: UserAuthority): Account =
        if (this.authorities.map { it.authority }.contains(authority)) {
            this
        } else {
            this.copy(authorities = this.authorities + AccountAuthority(authority, Instant.now()))
        }

    /**
     * Grants a user only the authorities in the list provided. Any existing authorities not in the list are revoked.
     */
    fun withAuthorities(newAuthorities: List<UserAuthority>): Account {
        val updated = this.copy(authorities = this.authorities.filter { newAuthorities.contains(it.authority) })

        return newAuthorities.fold(updated) { account, authority ->
            account.grantAuthority(authority)
        }
    }
}

data class AccountCreationEvent(val src: Any, val account: Account) : ApplicationEvent(src)

data class NotificationSettings(
    val weeklyActivity: Boolean,
    val emailOnAccountChange: Boolean
) {
    companion object {
        val default: NotificationSettings =
            NotificationSettings(weeklyActivity = false, emailOnAccountChange = true)

    }
}
