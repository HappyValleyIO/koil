package org.koil.admin

import org.koil.admin.accounts.UpdateAccountRequest
import org.koil.auth.UserAuthority
import org.koil.user.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

interface AdminService {
    fun createAdminFromEmail(email: String, password: HashedPassword): UserCreationResult

    fun getAccounts(queryingAsAccount: Long, pageable: Pageable): Page<Account>

    fun getAccount(queryingAsAccount: Long, accountId: Long): Account?

    fun updateAccount(requestor: Long, userToUpdate: Long, request: UpdateAccountRequest): AdminAccountUpdateResult
}

@Component
class AdminServiceImpl(
    private val userService: UserService,
    private val accountRepository: AccountRepository,
    @Value("\${admin-user.email:}") private val adminEmailFromEnv: String,
    @Value("\${admin-user.password:}") private val adminPasswordFromEnv: String,
) : AdminService, ApplicationListener<ContextRefreshedEvent> {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(AdminServiceImpl::class.java)
    }

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        if ((adminEmailFromEnv.isNotEmpty() && adminPasswordFromEnv.isNotEmpty())
            && accountRepository.findAccountByEmailAddressIgnoreCase(adminEmailFromEnv) == null
        ) {
            createAdminFromEmail(adminEmailFromEnv, HashedPassword.encode(adminPasswordFromEnv))
        }
    }

    override fun createAdminFromEmail(email: String, password: HashedPassword): UserCreationResult {
        return userService.createUser(
            UserCreationRequest(
                "Default Admin",
                email,
                password,
                "DefaultAdmin",
                listOf(UserAuthority.ADMIN)
            )
        ).also {
            if (it is UserCreationResult.CreatedUser) {
                LOGGER.info("Created an admin account with email {}", email)
            }
        }
    }

    override fun getAccounts(queryingAsAccount: Long, pageable: Pageable): Page<Account> {
        checkAdminStatus(queryingAsAccount)

        return accountRepository.findAll(pageable)
    }

    override fun getAccount(queryingAsAccount: Long, accountId: Long): Account? {
        checkAdminStatus(queryingAsAccount)

        return accountRepository.findByIdOrNull(accountId)
    }

    @Transactional
    override fun updateAccount(
        requestor: Long,
        userToUpdate: Long,
        request: UpdateAccountRequest
    ): AdminAccountUpdateResult {
        checkAdminStatus(requestor)

        val account: Account = accountRepository.findByIdOrNull(userToUpdate)
            ?: return AdminAccountUpdateResult.CouldNotFindAccount
        val emailInUse = accountRepository.existsAccountByEmailAddressIgnoreCase(request.normalizedEmail)

        return if (!emailInUse || account.emailAddress == request.normalizedEmail) {
            val updated = request.update(account)
            val saved = accountRepository.save(updated)
            AdminAccountUpdateResult.AccountUpdateSuccess(saved)
        } else {
            AdminAccountUpdateResult.EmailAlreadyTaken(account)
        }
    }

    private fun checkAdminStatus(queryingAsAccount: Long) {
        val isAdmin = accountRepository.findByIdOrNull(queryingAsAccount)?.isAdmin() ?: false

        require(isAdmin) {
            "Attempting to retrieve account as a non-admin user!"
        }
    }
}
