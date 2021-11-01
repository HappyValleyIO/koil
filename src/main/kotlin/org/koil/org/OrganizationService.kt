package org.koil.org

import org.koil.admin.AdminServiceImpl
import org.koil.auth.UserAuthority
import org.koil.dashboard.org.accounts.UpdateAccountRequest
import org.koil.user.Account
import org.koil.user.AccountRepository
import org.koil.user.UserCreationResult
import org.koil.user.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

interface OrganizationService {
    fun setupOrganization(request: OrganizationSetupRequest): OrganizationCreatedResult
    fun getAllOrganizations(): List<Organization>
    fun getOrganizationDetails(queryingAsAccount: Long): Organization
    fun getAccounts(queryingAsAccount: Long, pageable: Pageable): Page<Account>
    fun getAccount(queryingAsAccount: Long, accountId: Long): Account?
    fun updateAccount(requestor: Long, userToUpdate: Long, request: UpdateAccountRequest): OrgAccountUpdateResult
}

@Component
class OrganizationServiceImpl(
    private val organizationRepository: OrganizationRepository,
    private val accountRepository: AccountRepository,
) : OrganizationService {

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(OrganizationServiceImpl::class.java)
    }

    override fun setupOrganization(request: OrganizationSetupRequest): OrganizationCreatedResult {
        return try {
            createOrganizationAndUser(request)
        } catch (exception: Exception) {
            LOGGER.error("Failed to setup organization and admin user with exception [$exception]", exception)
            OrganizationCreatedResult.CreationFailed
        }
    }

    override fun getAllOrganizations(): List<Organization> {
        return organizationRepository.findAll().toList()
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    fun createOrganizationAndUser(request: OrganizationSetupRequest): OrganizationCreatedResult {
        val emailInUse = (accountRepository.findAccountByEmailAddressIgnoreCase(request.email) != null)
        if (emailInUse) {
            return OrganizationCreatedResult.UserCreationFailed(UserCreationResult.UserAlreadyExists)
        }

        val organization = organizationRepository.save(request.toOrganization())

        val userToCreate = request
            .toUserCreationRequest(organization.signupLink)
            .toAccount(organizationId = organization.organizationIdOrThrow())
        val createdUser = accountRepository.save(userToCreate)

        return OrganizationCreatedResult.CreatedOrganization(organization, createdUser)
    }

    override fun getOrganizationDetails(queryingAsAccount: Long): Organization {
        val account = getAccount(queryingAsAccount)
        validateOrgOwnerStatus(account)
        val organization = organizationRepository.findOrganizationByOrganizationId(account.organizationId)
        require(organization != null) {
            "The organization was not found for this account. That should be impossible- every account must have a valid organization linked to it."
        }
        return organization
    }

    @Transactional
    override fun updateAccount(
        requestor: Long,
        userToUpdate: Long,
        request: UpdateAccountRequest
    ): OrgAccountUpdateResult {
        val requestorAccount = getAccount(requestor)

        require(requestorAccount.isAdmin() || requestorAccount.isOrganizationOwner()) {
            "Attempting to update an account as a non-admin user!"
        }

        val account: Account = accountRepository.findByIdOrNull(userToUpdate)
            ?: return OrgAccountUpdateResult.CouldNotFindAccount

        if (!requestorAccount.isAdmin()) {
            require(account.organizationId == requestorAccount.organizationId) {
                "Attempting to update an account for an organization the user does not belong to."
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
        validateOrgOwnerStatus(account)
        return accountRepository.findAccountsByOrganizationId(account.organizationId, pageable)

    }

    override fun getAccount(queryingAsAccount: Long, accountId: Long): Account? {
        val account = getAccount(queryingAsAccount)

        validateOrgOwnerStatus(account)

        val lookupAccount = accountRepository.findByIdOrNull(accountId)

        if (!account.isAdmin()) {
            require(lookupAccount == null || lookupAccount.organizationId == account.organizationId) {
                "Attempting to retrieve an account for an organization that the user does not belong to!"
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

    private fun validateOrgOwnerStatus(account: Account?) {
        val isOrganizationOwner = account?.isOrganizationOwner() ?: false

        require(isOrganizationOwner) {
            "Attempting to retrieve account as a non-admin user!"
        }
    }
}
