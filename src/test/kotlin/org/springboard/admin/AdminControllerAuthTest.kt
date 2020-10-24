package org.springboard.admin

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springboard.auth.AuthAuthority
import org.springboard.testUserDetails
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminControllerAuthTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `GIVEN an admin user WHEN requesting the admin dashboard THEN return OK`() {
        val admin = testUserDetails(authorities = listOf(AuthAuthority.ADMIN))
        mockMvc.perform(get("/admin").with(user(admin)))
                .andExpect(status().isOk)
    }

    @Test
    fun `GIVEN a non-admin user WHEN requesting the admin dashboard THEN return OK`() {
        val nonAdmin = testUserDetails(authorities = listOf(AuthAuthority.USER))
        mockMvc.perform(get("/admin").with(user(nonAdmin)))
                .andExpect(status().isForbidden)
    }
}
