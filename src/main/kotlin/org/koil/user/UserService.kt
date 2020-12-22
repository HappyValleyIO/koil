package org.koil.user

import org.hibernate.validator.constraints.Length
import org.koil.auth.AuthAuthority
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
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
        private val persistence: UserPersistence,
        private val encoder: PasswordEncoder,
        private val publisher: ApplicationEventPublisher
) : UserService, UserDetailsService {
    override fun createUser(request: UserCreationRequest): UserCreationResult {
        return if (persistence.getUserByEmail(request.email) == null) {
            val user = persistence.createUser(UserToStore(request.fullName, request.email, request.getPassword(encoder), request.handle, request.authorities))
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
