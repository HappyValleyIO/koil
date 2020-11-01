package org.springboard.admin

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springboard.auth.AuthAuthority
import org.springboard.user.UserCreationRequest
import org.springboard.user.UserCreationResult
import org.springboard.user.UserServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AdminServiceTest {

    @Autowired
    lateinit var adminService: IAdminService

    @Autowired
    lateinit var adminPersistence: IAdminPersistence

    @Autowired
    lateinit var userDetails: UserServiceImpl

    @Test
    fun `GIVEN no existing admin user WHEN attempting to create admin THEN return success`() {
        val email = "user+${Random().nextInt()}@getspringboard.dev"
        val result = adminService.createAdminFromEmail(email, "SomePass123!")

        assertTrue(result is UserCreationResult.CreatedUser) {
            "Admin was successfully created"
        }

        assertEquals(listOf("ADMIN"), userDetails.loadUserByUsername(email)!!.authorities.map { it.authority })
    }

    @Test
    fun `GIVEN an admin user exists WHEN attempting to create admin THEN fail`() {
        val email = "user+${Random().nextInt()}@getspringboard.dev"
        adminService.createAdminFromEmail(email, "SomePass123!")
        val result = adminService.createAdminFromEmail(email, "SomePass123!")

        assertTrue(result is UserCreationResult.UserAlreadyExists) {
            "Admin already exists"
        }
    }

    @Test
    fun `GIVEN existing accounts WHEN querying for all accounts as an admin THEN return them`() {
        val email = "user+${Random().nextInt()}@getspringboard.dev"
        val admin = (adminService.createAdminFromEmail(email, "SomePass123!") as UserCreationResult.CreatedUser).account

        val result = adminService.getAccounts(admin.accountId)

        assertThat(result).isEqualTo(adminPersistence.getAllAccounts())
    }


    @Test
    fun `GIVEN existing accounts WHEN querying for all accounts as a non-admin THEN throw an error`() {
        val email = "user+${Random().nextInt()}@getspringboard.dev"
        (adminService.createAdminFromEmail(email, "SomePass123!") as UserCreationResult.CreatedUser).account

        val nonAdmin = userDetails.createUser(UserCreationRequest("Stephen the tester", "x$email", "TestPass23!", "tester", listOf(AuthAuthority.USER))) as UserCreationResult.CreatedUser

        assertThrows(IllegalArgumentException::class.java) {
            adminService.getAccounts(nonAdmin.account.accountId)
        }
    }

    @Test
    fun `GIVEN existing accounts WHEN querying for all accounts as a non-existent user THEN throw an error`() {
        val email = "user+${Random().nextInt()}@getspringboard.dev"
        (adminService.createAdminFromEmail(email, "SomePass123!") as UserCreationResult.CreatedUser).account

        assertThrows(IllegalArgumentException::class.java) {
            adminService.getAccounts(1_000_000_000)
        }
    }
}
