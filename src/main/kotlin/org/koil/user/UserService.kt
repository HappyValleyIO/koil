package org.koil.user

import org.koil.org.OrganizationRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

interface UserService {
    fun createUser(request: UserCreationRequest): UserCreationResult
    fun findUserById(accountId: Long): Account?
    fun updateUser(accountId: Long, request: UpdateUserSettingsRequest): AccountUpdateResult
}

@Component
class UserServiceImpl(
    private val repository: AccountRepository,
    private val organizationRepository: OrganizationRepository
) : UserService {
    override fun createUser(request: UserCreationRequest): UserCreationResult {
        return if (repository.findAccountByEmailAddressIgnoreCase(request.email) == null) {
            val organizationId = organizationRepository.findOrganizationBySignupLink(request.signupLink)?.organizationId ?: return UserCreationResult.InvalidSignupLink
            val account = request.toAccount(organizationId).let { repository.save(it) }

            UserCreationResult.CreatedUser(account)
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
                val emailInUse = repository.existsAccountByEmailAddressIgnoreCase(request.normalizedEmail)

                if (emailInUse && request.normalizedEmail != account.emailAddress) {
                    AccountUpdateResult.EmailAlreadyInUse(request.email)
                } else {
                    request.update(account)
                        .let {
                            AccountUpdateResult.AccountUpdated(repository.save(it))
                        }
                }
            } ?: throw NoAccountFoundUnexpectedlyException(accountId)

}
