package org.koil.admin

import org.koil.admin.accounts.UpdateAccountRequest
import org.koil.auth.UserAuthority
import org.koil.org.OrganizationCreatedResult
import org.koil.org.OrganizationService
import org.koil.org.OrganizationSetupRequest
import org.koil.user.Account
import org.koil.user.AccountRepository
import org.koil.user.password.HashedPassword
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
    fun getAccounts(queryingAsAccount: Long, pageable: Pageable): Page<AccountEnriched>

    fun getAccount(queryingAsAccount: Long, accountId: Long): Account?

    fun updateAccount(requestor: Long, userToUpdate: Long, request: UpdateAccountRequest): AdminAccountUpdateResult
}

@Component
class AdminServiceImpl(
    private val accountRepository: AccountRepository,
    private val organizationService: OrganizationService,
    @Value("\${admin-organization.name:}") private val adminOrganizationName: String,
    @Value("\${admin-user.email:}") private val adminEmailFromEnv: String,
    @Value("\${admin-user.password:}") private val adminPasswordFromEnv: String,
) : AdminService, ApplicationListener<ContextRefreshedEvent> {

    init{
        require(adminOrganizationName.isNotEmpty()){
            "You cannot start up the application with an empty admin organization name. Set the admin-organization.name variable."
        }

        require(adminEmailFromEnv.isNotEmpty()){
            "You cannot start up the application with an empty admin email. Set the admin-user.email variable."
        }

        require(adminPasswordFromEnv.isNotEmpty()){
            "You cannot start up the application with an empty admin password. Set the admin-user.password variable."
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(AdminServiceImpl::class.java)
    }

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        if ((adminEmailFromEnv.isNotEmpty() && adminPasswordFromEnv.isNotEmpty())
            && accountRepository.findAccountByEmailAddressIgnoreCase(adminEmailFromEnv) == null
        ) {
            createDefaultAdminOrganization()
        }
    }

    private fun createDefaultAdminOrganization(): OrganizationCreatedResult {
        val organizationCreatedResult = organizationService.setupOrganization(
            OrganizationSetupRequest(
                organizationName = adminOrganizationName,
                fullName = "Default Admin",
                email = adminEmailFromEnv,
                password = HashedPassword.encode(adminPasswordFromEnv),
                handle = "DefaultAdmin",
                authorities = listOf(UserAuthority.ADMIN, UserAuthority.ORG_OWNER)
            )
        )

        if (organizationCreatedResult !is OrganizationCreatedResult.CreatedOrganization) {
            LOGGER.error("Failed to create initial Organization so initial Administrator will not be created. Organization Creation Result [$organizationCreatedResult]")
        }
        return organizationCreatedResult
    }

    override fun getAccounts(queryingAsAccount: Long, pageable: Pageable): Page<AccountEnriched> {
        validateAdminStatus(queryingAsAccount)

        val accounts = accountRepository.findAll(pageable)

        val organizationMap = organizationService.getAllOrganizations(accounts.toList().map { it.organizationId })
            .associate { it.organizationId to it.organizationName }

        return accounts.map {
            val orgName = organizationMap[it.organizationId]
            check(orgName != null) {
                "Attempting to match an account to the organization name it belongs to. The organization name was missing for the organizationId of the account. This should be completely impossible due to our db setup."
            }
            AccountEnriched(it, orgName)
        }
    }

    override fun getAccount(queryingAsAccount: Long, accountId: Long): Account? {
        validateAdminStatus(queryingAsAccount)
        return accountRepository.findByIdOrNull(accountId)
    }

    @Transactional
    override fun updateAccount(
        requestor: Long,
        userToUpdate: Long,
        request: UpdateAccountRequest
    ): AdminAccountUpdateResult {
        validateAdminStatus(requestor)

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

    private fun validateAdminStatus(queryingAsAccount: Long) {
        val account = accountRepository.findByIdOrNull(queryingAsAccount)
        val isAdmin = account?.isAdmin() ?: false

        require(isAdmin) {
            "Attempting to retrieve account as a non-admin user!"
        }
    }
}
