package org.koil.auth

import org.koil.user.AccountRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class DefaultUserDetailsService(
    private val repository: AccountRepository
) : UserDetailsService {
    override fun loadUserByUsername(email: String): EnrichedUserDetails {
        repository.findAccountByEmailAddressIgnoreCase(email)?.let { account ->
            return EnrichedUserDetails(
                account.accountId ?: throw RuntimeException("Unexpectedly null accountId for existing account!"),
                account.handle,
                User.builder()
                    .username(account.emailAddress)
                    .password(account.password.encodedPassword)
                    .authorities(account.authorities.map { it.authority.grantedAuthority })
                    .build(),
            )
        } ?: throw UsernameNotFoundException("Could not find user $email")
    }
}
