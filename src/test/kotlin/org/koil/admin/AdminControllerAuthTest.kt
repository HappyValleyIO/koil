package org.koil.admin

import org.junit.jupiter.api.Test
import org.koil.BaseIntegrationTest
import org.koil.auth.AuthAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AdminControllerAuthTest : BaseIntegrationTest() {

    @Test
    fun `GIVEN an admin user WHEN requesting the admin dashboard THEN return OK`() {
        withTestSession(authorities = listOf(AuthAuthority.ADMIN)) { admin ->
            mockMvc.perform(get("/admin").with(user(admin)))
                .andExpect(status().isOk)
        }
    }

    @Test
    fun `GIVEN a non-admin user WHEN requesting the admin dashboard THEN return OK`() {
        withTestSession(authorities = listOf(AuthAuthority.USER)) { nonAdmin ->
            mockMvc.perform(get("/admin").with(user(nonAdmin)))
                .andExpect(status().isForbidden)
        }
    }
}
