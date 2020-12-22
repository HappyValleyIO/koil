package org.koil.user

import org.koil.auth.AuthAuthority
import org.springframework.context.ApplicationEvent
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

interface InternalUser {
    val accountId: Long
    val email: String
    val fullName: String
    val handle: String
    val publicId: UUID
    val authorities: List<AuthAuthority>

    fun isAdmin(): Boolean = authorities.contains(AuthAuthority.ADMIN)

    fun toAccount(): Account = Account(accountId, fullName, email, handle, publicId)
}

data class Account(val accountId: Long, val fullName: String, val email: String, val handle: String, val publicId: UUID) {
    companion object {
        fun fromUser(user: InternalUser): Account =
                with(user) {
                    Account(accountId, fullName, email, handle, publicId)
                }
    }
}

data class AccountCreationEvent(val src: Any, val account: Account) : ApplicationEvent(src)

data class EnrichedUserDetails(val details: UserDetails, val accountId: Long, val handle: String) : UserDetails by details {
    fun isAdmin(): Boolean {
        return authorities.contains(AuthAuthority.ADMIN.grantedAuthority)
    }
}
