package org.koil.user

import org.junit.jupiter.api.Test
import org.koil.BaseIntegrationTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.post

class UserSettingsControllerTest : BaseIntegrationTest() {

    @Test
    internal fun `GIVEN a logged in user WHEN submitting a bad requests THE return a 404`() {
        withTestSession { session ->

            mockMvc.post("/dashboard/user-settings") {
                with(user(session))
                with(csrf())

                param("name", "")
                param("email", "invalid")
            }.andExpect {
                status {
                    is4xxClientError()
                }
            }
        }
    }
}
