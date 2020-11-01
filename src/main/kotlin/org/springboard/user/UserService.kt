package org.springboard.user

import org.hibernate.validator.constraints.Length
import org.springboard.auth.AuthAuthority
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.util.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

interface InternalUser {
    fun isAdmin(): Boolean = authorities.contains(AuthAuthority.ADMIN)

    fun toAccount(): Account = Account(accountId, fullName, email, handle, publicId)

    val accountId: Long
    val email: String
    val fullName: String
    val handle: String
    val publicId: UUID
    val authorities: List<AuthAuthority>
}

data class UserCreationRequest(
        @get:NotEmpty val fullName: String,
        @get:Email val email: String,
        @get:Length(min = 8) val password: String,
        val handle: String,
        val authorities: List<AuthAuthority> = listOf(AuthAuthority.USER)
)

data class Account(val accountId: Long, val fullName: String, val email: String, val handle: String, val publicId: UUID) {
    companion object {
        fun fromUser(user: InternalUser): Account =
                with(user) {
                    Account(accountId, fullName, email, handle, publicId)
                }
    }
}

data class AccountCreationEvent(val src: Any, val account: Account) : ApplicationEvent(src)

data class EnrichedUserDetails(val details: UserDetails, val accountId: Long, val handle: String) : UserDetails by details

sealed class UserCreationResult {
    data class CreatedUser(val account: Account) : UserCreationResult()
    object UserAlreadyExists : UserCreationResult()
}

interface UserService {
    fun createUser(request: UserCreationRequest): UserCreationResult
}

@Component
class UserServiceImpl(
        private val persistence: UserPersistence,
        private val encoder: PasswordEncoder,
        private val publisher: ApplicationEventPublisher
) : UserService, UserDetailsService {
    override fun createUser(request: UserCreationRequest): UserCreationResult {
        return if (persistence.getUserByEmail(request.email) == null) {
            val encoded = request.copy(password = encoder.encode(request.password))
            val user = persistence.createUser(encoded)
            val account = Account.fromUser(user)

            publisher.publishEvent(AccountCreationEvent(this, account))

            UserCreationResult.CreatedUser(account)
        } else {
            UserCreationResult.UserAlreadyExists
        }
    }

    override fun loadUserByUsername(email: String?): EnrichedUserDetails? {
        val user = if (email != null) persistence.getUserByEmail(email) else null
        if (user !== null) {
            return EnrichedUserDetails(
                    User.builder()
                            .username(user.email)
                            .password(user.password)
                            .authorities(user.authorities.map { SimpleGrantedAuthority(it.name) })
                            .build(), user.accountId, user.handle
            )
        } else {
            throw UsernameNotFoundException("Could not find user $email")
        }
    }
}
