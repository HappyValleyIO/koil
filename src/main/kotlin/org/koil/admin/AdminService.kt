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

    fun getAccounts(queryingAsAccount: Long, pageable: Pageable): Page<Account>

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

    override fun getAccounts(queryingAsAccount: Long, pageable: Pageable): Page<Account> {
        val account = getAccount(queryingAsAccount)

        require(account.isAdmin() || account.isCompanyOwner()) {
            "Attempting to retrieve accounts as a non-admin user!"
        }

        return if (account.isAdmin()) {
            validateAdminStatus(account)
            accountRepository.findAll(pageable)
        } else {
            validateCompanyOwnerStatus(account)
            accountRepository.findAccountsByCompanyId(account.companyId, pageable)
        }
    }

    override fun getAccount(queryingAsAccount: Long, accountId: Long): Account? {
        val account = getAccount(queryingAsAccount)

        require(account.isAdmin() || account.isCompanyOwner()) {
            "Attempting to retrieve another account as a non-admin user!"
        }

        validateCompanyOwnerStatus(account)


        val lookupAccount = accountRepository.findByIdOrNull(accountId)

        if (!account.isAdmin()) {
            require(lookupAccount == null || lookupAccount.companyId == account.companyId) {
                "Attempting to retrieve an account for a company that the user does not belong to!"
            }
        }

        return lookupAccount
    }

    @Transactional
    override fun updateAccount(
        requestor: Long,
        userToUpdate: Long,
        request: UpdateAccountRequest
    ): AdminAccountUpdateResult {
        val requestorAccount = getAccount(requestor)

        require(requestorAccount.isAdmin() || requestorAccount.isCompanyOwner()) {
            "Attempting to update an account as a non-admin user!"
        }

        val account: Account = accountRepository.findByIdOrNull(userToUpdate)
            ?: return AdminAccountUpdateResult.CouldNotFindAccount

        if(!requestorAccount.isAdmin()){
            require(account.companyId == requestorAccount.companyId){
                "Attempting to update an account for a company the user does not belong to."
            }
            require(!request.authorities.contains(UserAuthority.ADMIN)){
                "Non Admin attempting to escalate privileges to Admin role."
            }
        }

        val emailInUse = accountRepository.existsAccountByEmailAddressIgnoreCase(request.normalizedEmail)

        return if (!emailInUse || account.emailAddress == request.normalizedEmail) {
            val updated = request.update(account)
            val saved = accountRepository.save(updated)
            AdminAccountUpdateResult.AccountUpdateSuccess(saved)
        } else {
            AdminAccountUpdateResult.EmailAlreadyTaken(account)
        }
    }

    private fun getAccount(queryingAsAccount: Long): Account {
        val account = accountRepository.findByIdOrNull(queryingAsAccount)
        require(account != null) {
            "Could not locate the account for the accountId provided. This should be impossible."
        }
        return account
    }

    private fun validateCompanyOwnerStatus(account: Account?) {
        val isCompanyOwner = account?.isCompanyOwner() ?: false

        require(isCompanyOwner) {
            "Attempting to retrieve account as a non-admin user!"
        }
    }

    private fun validateAdminStatus(account: Account?) {
        val isAdmin = account?.isAdmin() ?: false

        require(isAdmin) {
            "Attempting to retrieve account as a non-admin user!"
        }
    }
}
