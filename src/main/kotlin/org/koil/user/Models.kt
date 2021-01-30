package org.koil.user

import org.koil.auth.AuthAuthority
import org.springframework.context.ApplicationEvent
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table
import org.springframework.security.core.userdetails.UserDetails
import java.time.Duration
import java.time.Instant
import java.util.*

data class AccountVerification(val verificationCode: UUID,
                               val createdAt: Instant,
                               val expiresAt: Instant)

data class AccountPasswordReset(val resetCode: UUID,
                                val createdAt: Instant = Instant.now(),
                                val expiresAt: Instant = Instant.now().plus(Duration.ofHours(3)))

@Table("account_authorities")
data class AccountAuthority(val authority: AuthAuthority, val authorityGranted: Instant)

@Table("accounts")
data class Account(@Id val accountId: Long?, val startDate: Instant, val fullName: String, val handle: String, val publicAccountId: UUID, val emailAddress: String, val password: String, val stopDate: Instant?,
                   @MappedCollection(idColumn = "account_id") val authorities: List<AccountAuthority>,
                   @MappedCollection(idColumn = "account_id") val accountVerification: AccountVerification? = null,
                   @MappedCollection(idColumn = "account_id") val accountPasswordReset: AccountPasswordReset? = null
) {
    fun isAdmin(): Boolean = authorities.map { it.authority }.contains(AuthAuthority.ADMIN)

    fun withPasswordReset(code: UUID): Account =
            this.copy(accountPasswordReset = AccountPasswordReset(code))

    fun updatePassword(encodedPassword: String): Account =
            this.copy(password = encodedPassword)
}

data class AccountCreationEvent(val src: Any, val account: Account) : ApplicationEvent(src)

data class EnrichedUserDetails(val details: UserDetails, val accountId: Long, val handle: String) : UserDetails by details {
    fun isAdmin(): Boolean {
        return authorities.contains(AuthAuthority.ADMIN.grantedAuthority)
    }
}
