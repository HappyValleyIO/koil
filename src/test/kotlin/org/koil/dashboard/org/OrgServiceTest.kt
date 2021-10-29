package org.koil.dashboard.org

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import net.bytebuddy.utility.RandomString
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.koil.BaseIntegrationTest
import org.koil.auth.UserAuthority
import org.koil.company.CompanyCreationResult
import org.koil.company.CompanyRepository
import org.koil.company.CompanySetupRequest
import org.koil.dashboard.org.accounts.UpdateAccountRequest
import org.koil.user.Account
import org.koil.user.UserCreationRequest
import org.koil.user.UserCreationResult
import org.koil.user.password.HashedPassword
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import java.util.*
import kotlin.streams.toList

class OrgServiceTest : BaseIntegrationTest() {

    @Autowired
    lateinit var orgService: OrgService

    @Autowired
    lateinit var companyRepository: CompanyRepository

    val password = HashedPassword.encode("SecurePass123!")

    @Test
    fun `GIVEN existing accounts WHEN querying for all accounts as an org owner THEN return all accounts for all companies`() {
        val createdCompany = createDummyTestCompany()
        createDummyTestCompany()

        val result = orgService.getAccounts(createdCompany.adminAccount.accountId!!, Pageable.unpaged())

        assertThat(result.get().toList().sortedBy { it.accountId }).isEqualTo(
            accountRepository.findAll().toList().filter{it.companyId == createdCompany.adminAccount.companyId}.sortedBy { it.accountId })
    }

    @Test
    fun `GIVEN existing accounts WHEN querying for all accounts as a non-admin THEN throw an error`() {
        val email = "user+${Random().nextInt()}@getkoil.dev"
        val signupLink = createDummyTestCompany().company.signupLink

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
            orgService.getAccounts(nonAdmin.account.accountId!!, Pageable.unpaged())
        }
    }

    @Test
    fun `GIVEN existing accounts WHEN querying for all accounts as a non-existent user THEN throw an error`() {
        assertThrows(IllegalArgumentException::class.java) {
            orgService.getAccounts(Long.MAX_VALUE, Pageable.unpaged())
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

            val result = (orgService.updateAccount(
                admin.accountId!!,
                account.accountId!!,
                update
            ) as OrgAccountUpdateResult.AccountUpdateSuccess).account

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

            val result = orgService.updateAccount(
                admin.accountId!!,
                account.accountId!!,
                update
            )

            assertThat(result).isEqualTo(OrgAccountUpdateResult.EmailAlreadyTaken(account))
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

            val result = orgService.updateAccount(
                admin.accountId!!,
                account.accountId!!,
                update
            )

            val authorities = (result as OrgAccountUpdateResult.AccountUpdateSuccess).account.authorities

            assertThat(result).isEqualTo(OrgAccountUpdateResult.AccountUpdateSuccess(account.copy(authorities = authorities)))
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
                orgService.updateAccount(
                    account.accountId!!,
                    admin.accountId!!,
                    update
                )
            }
        }
    }

    private fun getAdminUser(): Account {
        return accountRepository.findAll().first { it.isAdmin() }!!
    }

    private fun createDummyTestCompany(): CompanyCreationResult.CreatedCompany {
        val companyCreationResult = companyService.setupCompany(
            CompanySetupRequest(
                companyName = "TestCompany",
                fullName = "User Main",
                email = "user${RandomString.make()}@getkoil.dev",
                password = HashedPassword.encode("Password123!"),
                handle = RandomString.make()
            )
        )
        return (companyCreationResult as CompanyCreationResult.CreatedCompany)
    }
}
