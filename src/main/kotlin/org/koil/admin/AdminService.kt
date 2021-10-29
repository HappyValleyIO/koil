package org.koil.admin

import org.koil.admin.accounts.UpdateAccountRequest
import org.koil.auth.UserAuthority
import org.koil.company.CompanyCreationResult
import org.koil.company.CompanyService
import org.koil.company.CompanySetupRequest
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

    fun createDefaultAdminCompany(companyName: String): CompanyCreationResult

    fun getAccounts(queryingAsAccount: Long, pageable: Pageable): Page<AccountEnriched>

    fun getAccount(queryingAsAccount: Long, accountId: Long): Account?

    fun updateAccount(requestor: Long, userToUpdate: Long, request: UpdateAccountRequest): AdminAccountUpdateResult
}

@Component
class AdminServiceImpl(
    private val accountRepository: AccountRepository,
    private val companyService: CompanyService,
    @Value("\${admin-company.name:}") private val adminCompanyName: String,
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
            createDefaultAdminCompany(adminCompanyName)
        }
    }

    override fun createDefaultAdminCompany(companyName: String): CompanyCreationResult {
        val companyCreationResult = companyService.setupCompany(
            CompanySetupRequest(
                companyName = adminCompanyName,
                fullName = "Default Admin",
                email = adminEmailFromEnv,
                password = HashedPassword.encode(adminPasswordFromEnv),
                handle = "DefaultAdmin",
                authorities = listOf(UserAuthority.ADMIN, UserAuthority.COMPANY_OWNER)
            )
        )

        if (companyCreationResult !is CompanyCreationResult.CreatedCompany) {
            LOGGER.error("Failed to create initial Company so initial Administrator will not be created. Company Creation Result [$companyCreationResult]")
        }
        return companyCreationResult
    }

    override fun getAccounts(queryingAsAccount: Long, pageable: Pageable): Page<AccountEnriched> {
        validateAdminStatus(queryingAsAccount)

        val accounts = accountRepository.findAll(pageable)

        val companyNameMap = companyService.getAllCompanies().associate { it.companyId to it.companyName }

        return accounts.map {
            val companyName = companyNameMap[it.companyId]
            check(companyName != null) {
                "Attempting to match an account to the company name it belongs to. The company name was missing for the companyId of the account. This should be completely impossible due to our db setup."
            }
            AccountEnriched(it, companyName)
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
