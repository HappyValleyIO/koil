package org.koil.dashboard.org

import org.junit.jupiter.api.Test
import org.koil.BaseIntegrationTest
import org.koil.auth.UserAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class OrgControllerAuthTest : BaseIntegrationTest() {

    @Test
    fun `GIVEN a ORG_OWNER user WHEN requesting the admin dashboard THEN return OK`() {
        withTestSession(authorities = listOf(UserAuthority.ORG_OWNER)) { admin ->
            mockMvc.perform(get("/dashboard/org").with(user(admin)))
                .andExpect(status().isOk)
        }
    }

    @Test
    fun `GIVEN a USER user WHEN requesting the admin dashboard THEN return OK`() {
        withTestSession(authorities = listOf(UserAuthority.USER)) { admin ->
            mockMvc.perform(get("/dashboard/org").with(user(admin)))
                .andExpect(status().isForbidden)
        }
    }

    @Test
    fun `GIVEN an ADMIN user WHEN requesting the admin dashboard THEN return OK`() {
        withTestSession(authorities = listOf(UserAuthority.ADMIN)) { admin ->
            mockMvc.perform(get("/dashboard/org").with(user(admin)))
                .andExpect(status().isForbidden)
        }
    }

}
