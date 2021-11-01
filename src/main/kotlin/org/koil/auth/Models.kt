package org.koil.auth

import org.springframework.security.core.userdetails.UserDetails

data class EnrichedUserDetails(
    val accountId: Long,
    val handle: String,
    private val details: UserDetails,
) : UserDetails by details {
    fun isAdmin(): Boolean {
        return details.authorities.map { it.authority }.contains(UserAuthority.ADMIN.name)
    }
}
