package org.koil.user

import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

interface UserService {
    fun createUser(request: UserCreationRequest): UserCreationResult
    fun findUserById(accountId: Long): Account?
    fun updateUser(accountId: Long, request: UpdateUserSettingsRequest): AccountUpdateResult
}

@Component
class UserServiceImpl(
    private val repository: AccountRepository,
    private val publisher: ApplicationEventPublisher
) : UserService {
    override fun createUser(request: UserCreationRequest): UserCreationResult {
        return if (repository.findAccountByEmailAddressIgnoreCase(request.email) == null) {
            val authorities = request.authorities.map { AccountAuthority(it, Instant.now()) }
            val account = Account(
                null,
                Instant.now(),
                request.fullName,
                request.handle,
                UUID.randomUUID(),
                request.email,
                request.password,
                null,
                notificationSettings = NotificationSettings.default,
                authorities
            )
            val saved = repository.save(account)

            publisher.publishEvent(AccountCreationEvent(this, account))

            UserCreationResult.CreatedUser(saved)
        } else {
            UserCreationResult.UserAlreadyExists
        }
    }

    override fun findUserById(accountId: Long): Account? =
        repository.findByIdOrNull(accountId)

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    override fun updateUser(accountId: Long, request: UpdateUserSettingsRequest): AccountUpdateResult =
        repository.findByIdOrNull(accountId)
            ?.let { account ->
                val updated = request.update(account)
                val emailInUse = repository.existsAccountByEmailAddressIgnoreCase(request.normalizedEmail)

                if (emailInUse && request.normalizedEmail != account.emailAddress) {
                    AccountUpdateResult.EmailAlreadyInUse(request.email)
                } else {
                    repository.save(updated)
                        .let {
                            AccountUpdateResult.AccountUpdated(it)
                        }
                }
            } ?: throw NoAccountFoundUnexpectedlyException(accountId)

}
