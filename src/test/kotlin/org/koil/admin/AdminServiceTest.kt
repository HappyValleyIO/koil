package org.koil.admin

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import net.bytebuddy.utility.RandomString
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Isolated
import org.koil.BaseIntegrationTest
import org.koil.admin.accounts.UpdateAccountRequest
import org.koil.auth.UserAuthority
import org.koil.org.OrganizationCreatedResult
import org.koil.org.OrganizationRepository
import org.koil.org.OrganizationSetupRequest
import org.koil.user.Account
import org.koil.user.UserCreationRequest
import org.koil.user.UserCreationResult
import org.koil.user.password.HashedPassword
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import java.util.*
import kotlin.streams.toList

class AdminServiceTest : BaseIntegrationTest() {

    @Autowired
    lateinit var adminService: AdminService

    @Autowired
    lateinit var organizationRepository: OrganizationRepository

    val password = HashedPassword.encode("SecurePass123!")

    @Test
    fun `GIVEN existing accounts WHEN querying for all accounts an ORG_OWNER THEN an illegal argument exception is thrown`() {
        val createdOrg = createDummyTestOrganization()

        assertThrows(IllegalArgumentException::class.java) {
            adminService.getAccounts(createdOrg.adminAccount.accountId!!, Pageable.unpaged())
        }
    }

    @Test
    fun `GIVEN existing accounts WHEN querying for all accounts as an admin THEN return all accounts for all companies`() {
        val admin = getAdminUser()
        createDummyTestOrganization()
        createDummyTestOrganization()

        val result = adminService.getAccounts(admin.accountId!!, Pageable.unpaged())

        val organizations = organizationRepository.findAll().associate { it.organizationId to it.organizationName }
        assertThat(result.get().toList().sortedBy { it.account.accountId }).isEqualTo(
            accountRepository.findAll().sortedBy { it.accountId }
                .map { AccountEnriched(it, organizations[it.organizationId]!!) })
    }

    @Test
    fun `GIVEN existing accounts WHEN querying for all accounts as a non-admin THEN throw an error`() {
        val email = "user+${Random().nextInt()}@getkoil.dev"
        val signupLink = createDummyTestOrganization().organization.signupLink

        val nonAdmin = userService.createUser(
            UserCreationRequest(
                signupLink,
                "Stephen the tester",
                "x$email",
                password,
                "tester",
                listOf(UserAuthority.USER)
            )
        ) as UserCreationResult.CreatedUser

        assertThrows(IllegalArgumentException::class.java) {
            adminService.getAccounts(nonAdmin.account.accountId!!, Pageable.unpaged())
        }
    }

    @Test
    fun `GIVEN existing accounts WHEN querying for all accounts as a non-existent user THEN throw an error`() {
        assertThrows(IllegalArgumentException::class.java) {
            adminService.getAccounts(Long.MAX_VALUE, Pageable.unpaged())
        }
    }

    @Test
    internal fun `GIVEN existing account WHEN updating with available details THEN successfully update`() {
        val admin = getAdminUser()
        withTestAccount() { account ->
            val update = UpdateAccountRequest(
                "Updated Name",
                "updated${account.emailAddress}  ",
                "u${account.handle}",
                UserAuthority.values().toList()
            )

            val result = (adminService.updateAccount(
                admin.accountId!!,
                account.accountId!!,
                update
            ) as AdminAccountUpdateResult.AccountUpdateSuccess).account

            assertThat(result.handle).isEqualTo(update.handle)
            assertThat(result.fullName).isEqualTo(update.fullName)
            assertThat(result.emailAddress).isEqualTo(update.normalizedEmail)
            assertThat(result.authorities.map { it.authority }).containsOnly(*(update.authorities.toTypedArray()))
        }
    }

    @Test
    internal fun `GIVEN existing account WHEN updating with taken email THEN return failure`() {
        val admin = getAdminUser()

        withTestAccount { account ->
            val update = UpdateAccountRequest(
                "Updated Name",
                "admin@getkoil.dev",
                "u${account.handle}",
                UserAuthority.values().toList()
            )

            val result = adminService.updateAccount(
                admin.accountId!!,
                account.accountId!!,
                update
            )

            assertThat(result).isEqualTo(AdminAccountUpdateResult.EmailAlreadyTaken(account))
        }
    }

    @Test
    internal fun `GIVEN existing account WHEN updating with nothing changed THEN return success`() {
        val admin = getAdminUser()

        withTestAccount { account ->
            val update = UpdateAccountRequest(
                account.fullName,
                account.emailAddress,
                account.handle,
                account.authorities.map { it.authority }
            )

            val result = adminService.updateAccount(
                admin.accountId!!,
                account.accountId!!,
                update
            )

            val authorities = (result as AdminAccountUpdateResult.AccountUpdateSuccess).account.authorities

            assertThat(result).isEqualTo(AdminAccountUpdateResult.AccountUpdateSuccess(account.copy(authorities = authorities)))
            assertThat(authorities.map { it.authority }).isEqualTo(account.authorities.map { it.authority })
        }
    }


    @Test
    internal fun `GIVEN existing account WHEN updating as non-admin THEN fail`() {
        val admin = getAdminUser()

        withTestAccount { account ->
            val update = UpdateAccountRequest(
                account.fullName,
                account.emailAddress,
                account.handle,
                account.authorities.map { it.authority }
            )

            assertThrows(IllegalArgumentException::class.java) {
                adminService.updateAccount(
                    account.accountId!!,
                    admin.accountId!!,
                    update
                )
            }
        }
    }

    private fun getAdminUser(): Account {
        return accountRepository.findAll().filter { it.isAdmin() }.first()!!
    }

    private fun createDummyTestOrganization(): OrganizationCreatedResult.CreatedOrganization {
        val organizationCreatedResult = organizationService.setupOrganization(
            OrganizationSetupRequest(
                organizationName = "TestCompany",
                fullName = "User Main",
                email = "user${RandomString.make()}@getkoil.dev",
                password = HashedPassword.encode("Password123!"),
                handle = RandomString.make()
            )
        )
        return (organizationCreatedResult as OrganizationCreatedResult.CreatedOrganization)
    }
}
