package org.koil.dashboard.org

import org.koil.auth.UserAuthority
import org.koil.company.Company
import org.koil.company.CompanyRepository
import org.koil.dashboard.org.accounts.UpdateAccountRequest
import org.koil.user.Account
import org.koil.user.AccountRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

interface OrgService {
    fun getCompanyDetails(queryingAsAccount: Long): Company
    fun getAccounts(queryingAsAccount: Long, pageable: Pageable): Page<Account>
    fun getAccount(queryingAsAccount: Long, accountId: Long): Account?
    fun updateAccount(requestor: Long, userToUpdate: Long, request: UpdateAccountRequest): OrgAccountUpdateResult
}

@Component
class OrgServiceImpl(
    private val accountRepository: AccountRepository,
    private val companyRepository: CompanyRepository
) : OrgService {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(OrgServiceImpl::class.java)
    }

    override fun getCompanyDetails(queryingAsAccount: Long): Company {
        val account = getAccount(queryingAsAccount)
        validateCompanyOwnerStatus(account)
        val company = companyRepository.findCompanyByCompanyId(account.companyId)
        require(company != null) {
            "The company was not found for this account. That should be impossible- every account must have a valid company linked to it."
        }
        return company
    }

    @Transactional
    override fun updateAccount(
        requestor: Long,
        userToUpdate: Long,
        request: UpdateAccountRequest
    ): OrgAccountUpdateResult {
        val requestorAccount = getAccount(requestor)

        require(requestorAccount.isAdmin() || requestorAccount.isCompanyOwner()) {
            "Attempting to update an account as a non-admin user!"
        }

        val account: Account = accountRepository.findByIdOrNull(userToUpdate)
            ?: return OrgAccountUpdateResult.CouldNotFindAccount

        if (!requestorAccount.isAdmin()) {
            require(account.companyId == requestorAccount.companyId) {
                "Attempting to update an account for a company the user does not belong to."
            }
            require(!request.authorities.contains(UserAuthority.ADMIN)) {
                "Non Admin attempting to escalate privileges to Admin role."
            }
        }

        val emailInUse = accountRepository.existsAccountByEmailAddressIgnoreCase(request.normalizedEmail)

        return if (!emailInUse || account.emailAddress == request.normalizedEmail) {
            val updated = request.update(account)
            val saved = accountRepository.save(updated)
            OrgAccountUpdateResult.AccountUpdateSuccess(saved)
        } else {
            OrgAccountUpdateResult.EmailAlreadyTaken(account)
        }
    }


    override fun getAccounts(queryingAsAccount: Long, pageable: Pageable): Page<Account> {
        val account = getAccount(queryingAsAccount)
        validateCompanyOwnerStatus(account)
        return accountRepository.findAccountsByCompanyId(account.companyId, pageable)

    }

    override fun getAccount(queryingAsAccount: Long, accountId: Long): Account? {
        val account = getAccount(queryingAsAccount)

        validateCompanyOwnerStatus(account)

        val lookupAccount = accountRepository.findByIdOrNull(accountId)

        if (!account.isAdmin()) {
            require(lookupAccount == null || lookupAccount.companyId == account.companyId) {
                "Attempting to retrieve an account for a company that the user does not belong to!"
            }
        }

        return lookupAccount
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
