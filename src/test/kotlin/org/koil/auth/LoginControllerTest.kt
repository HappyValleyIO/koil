package org.koil.auth

import org.junit.jupiter.api.Test
import org.koil.BaseIntegrationTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.get

class LoginControllerTest : BaseIntegrationTest() {
    companion object {
        const val loginEndpoint = "/auth/login"
    }

    @Test
    internal fun `GIVEN user is already logged in WHEN visiting login page THEN redirect to dashboard`() {
        withTestSession { session ->
            mockMvc.get(loginEndpoint) {
                with(SecurityMockMvcRequestPostProcessors.user(session))
            }.andExpect {
                status {
                    is3xxRedirection()
                }

                header {
                    string("location", "/dashboard")
                }
            }
        }
    }
}
