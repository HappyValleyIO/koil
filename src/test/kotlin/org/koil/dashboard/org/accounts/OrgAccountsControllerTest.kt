package org.koil.dashboard.org.accounts

import org.junit.jupiter.api.Test
import org.koil.BaseIntegrationTest
import org.koil.auth.UserAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.post

class OrgAccountsControllerTest : BaseIntegrationTest() {
    @Test
    internal fun `bad inputs result in a 400 error`() {
        withTestSession(authorities = listOf(UserAuthority.COMPANY_OWNER)) { admin ->
            withTestAccount { account ->
                mockMvc.post("/dashboard/org/accounts/${admin.accountId}") {
                    with(csrf())
                    with(user(admin))

                    param("fullName", "")
                    param("email", account.emailAddress)
                    param("handle", account.handle)
                    param("authorities", account.authorities.map { it.authority }.joinToString(","))
                }
                    .andExpect {
                        status {
                            is4xxClientError()
                        }
                    }
            }
        }
    }
}
