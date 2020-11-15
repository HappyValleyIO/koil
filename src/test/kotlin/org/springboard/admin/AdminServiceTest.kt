package org.springboard.admin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
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
    lateinit var adminService: AdminServiceImpl

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

}
