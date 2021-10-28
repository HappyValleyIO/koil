package org.koil.auth

import org.springframework.security.core.userdetails.UserDetails

data class EnrichedUserDetails(
    val accountId: Long,
    val handle: String,
    val isAdmin: Boolean,
    private val details: UserDetails,
) : UserDetails by details
