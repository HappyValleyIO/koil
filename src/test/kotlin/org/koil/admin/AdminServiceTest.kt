package org.koil.admin

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.koil.BaseIntegrationTest
import org.koil.admin.accounts.UpdateAccountRequest
import org.koil.auth.UserAuthority
import org.koil.user.HashedPassword
import org.koil.user.UserCreationRequest
import org.koil.user.UserCreationResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import java.util.*

class AdminServiceTest : BaseIntegrationTest() {

    @Autowired
    lateinit var adminService: AdminService

    val password = HashedPassword.encode("SecurePass123!")

    @Test
    fun `GIVEN no existing admin user WHEN attempting to create admin THEN return success`() {
        val email = "user+${Random().nextInt()}@getkoil.dev"
        val result = adminService.createAdminFromEmail(email, password)

        assertTrue(result is UserCreationResult.CreatedUser) {
            "Admin was successfully created"
        }

        assertEquals(listOf("ADMIN"), userDetailsService.loadUserByUsername(email).authorities.map { it.authority })
    }

    @Test
    fun `GIVEN an admin user exists WHEN attempting to create admin THEN fail`() {
        val email = "user+${Random().nextInt()}@getkoil.dev"
        adminService.createAdminFromEmail(email, password)
        val result = adminService.createAdminFromEmail(email, password)

        assertTrue(result is UserCreationResult.UserAlreadyExists) {
            "Admin already exists"
        }
    }

    @Test
    fun `GIVEN existing accounts WHEN querying for all accounts as an admin THEN return them`() {
        val email = "user+${Random().nextInt()}@getkoil.dev"
        val admin = (adminService.createAdminFromEmail(email, password) as UserCreationResult.CreatedUser).account

        val result = adminService.getAccounts(admin.accountId!!, Pageable.unpaged())

        assertThat(result).isEqualTo(accountRepository.findAll(Pageable.unpaged()))
    }


    @Test
    fun `GIVEN existing accounts WHEN querying for all accounts as a non-admin THEN throw an error`() {
        val email = "user+${Random().nextInt()}@getkoil.dev"
        (adminService.createAdminFromEmail(email, password) as UserCreationResult.CreatedUser).account

        val nonAdmin = userService.createUser(
            UserCreationRequest(
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
        val email = "user+${Random().nextInt()}@getkoil.dev"
        (adminService.createAdminFromEmail(email, password) as UserCreationResult.CreatedUser).account

        assertThrows(IllegalArgumentException::class.java) {
            adminService.getAccounts(Long.MAX_VALUE, Pageable.unpaged())
        }
    }

    @Test
    internal fun `GIVEN existing account WHEN updating with available details THEN successfully update`() {
        val email = "user+${Random().nextInt()}@getkoil.dev"
        val admin = (adminService.createAdminFromEmail(email, password) as UserCreationResult.CreatedUser).account

        withTestAccount { account ->
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
        val email = "user+${Random().nextInt()}@getkoil.dev"
        val admin =
            (adminService.createAdminFromEmail(email, password) as UserCreationResult.CreatedUser).account

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
        val email = "user+${Random().nextInt()}@getkoil.dev"
        val admin =
            (adminService.createAdminFromEmail(email, password) as UserCreationResult.CreatedUser).account

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
        val email = "user+${Random().nextInt()}@getkoil.dev"
        val admin =
            (adminService.createAdminFromEmail(email, password) as UserCreationResult.CreatedUser).account

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
}
