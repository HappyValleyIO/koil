package org.koil.admin

import org.koil.auth.AuthAuthority
import org.koil.user.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

interface IAdminService {
    fun createAdminFromEmail(email: String, password: String): UserCreationResult

    fun getAccounts(queryingAsAccount: Long): List<Account>
}

@Component
class AdminServiceImpl(
    private val userService: UserService,
    @Value("\${admin-user.email:}") private val adminEmailFromEnv: String,
    @Value("\${admin-user.password:}") private val adminPasswordFromEnv: String,
    private val persistence: IAdminPersistence,
    private val accountRepository: AccountRepository
) : IAdminService, ApplicationListener<ContextRefreshedEvent> {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(AdminServiceImpl::class.java)
    }

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        if (adminEmailFromEnv.isNotEmpty() && adminPasswordFromEnv.isNotEmpty()) {
            createAdminFromEmail(adminEmailFromEnv, adminPasswordFromEnv)
        }
    }

    override fun createAdminFromEmail(email: String, password: String): UserCreationResult {
        return userService.createUser(
            UserCreationRequest(
                "Default Admin",
                email,
                password,
                "DefaultAdmin",
                listOf(AuthAuthority.ADMIN)
            )
        ).also {
            if (it is UserCreationResult.CreatedUser) {
                LOGGER.info("Created an admin account with email {}", email)
            }
        }
    }

    override fun getAccounts(queryingAsAccount: Long): List<Account> {
        val isAdmin = accountRepository.findByIdOrNull(queryingAsAccount)?.isAdmin() ?: false

        require(isAdmin) {
            "Attempting to retrieve account as a non-admin user!"
        }

        return persistence.getAllAccounts()
    }
}
