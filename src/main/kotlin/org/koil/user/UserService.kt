package org.koil.user

import org.hibernate.validator.constraints.Length
import org.koil.auth.AuthAuthority
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

data class UserCreationRequest(
    @get:NotEmpty val fullName: String,
    @get:Email val email: String,
    @field:Length(min = 8) private val password: String,
    val handle: String,
    val authorities: List<AuthAuthority> = listOf(AuthAuthority.USER)
) {
    fun getPassword(encoder: PasswordEncoder): String = encoder.encode(password)
}

sealed class UserCreationResult {
    data class CreatedUser(val account: Account) : UserCreationResult()
    object UserAlreadyExists : UserCreationResult()
}

interface UserService {
    fun createUser(request: UserCreationRequest): UserCreationResult
}

@Component
class UserServiceImpl(
    private val repository: AccountRepository,
    private val encoder: PasswordEncoder,
    private val publisher: ApplicationEventPublisher
) : UserService, UserDetailsService {
    override fun createUser(request: UserCreationRequest): UserCreationResult {
        return if (repository.findAccountByEmailAddress(request.email) == null) {
            val authorities = request.authorities.map { AccountAuthority(it, Instant.now()) }
            val account = Account(
                null,
                Instant.now(),
                request.fullName,
                request.handle,
                UUID.randomUUID(),
                request.email,
                request.getPassword(encoder),
                null,
                authorities
            )
            val saved = repository.save(account)

            publisher.publishEvent(AccountCreationEvent(this, account))

            UserCreationResult.CreatedUser(saved)
        } else {
            UserCreationResult.UserAlreadyExists
        }
    }

    override fun loadUserByUsername(email: String?): EnrichedUserDetails? {
        val account = if (email != null) repository.findAccountByEmailAddress(email) else null
        if (account !== null && account.accountId !== null) {
            return EnrichedUserDetails(
                User.builder()
                    .username(account.emailAddress)
                    .password(account.password)
                    .authorities(account.authorities.map { it.authority.grantedAuthority })
                    .build(), account.accountId, account.handle
            )
        } else {
            throw UsernameNotFoundException("Could not find user $email")
        }
    }
}
