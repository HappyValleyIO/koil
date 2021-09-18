package org.koil.auth

import org.junit.jupiter.api.Test
import org.koil.BaseIntegrationTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

class PasswordResetControllerTest : BaseIntegrationTest() {
    @Test
    internal fun `GIVEN an existing user WHEN resetting password THEN succeed`() {
        withTestAccount { account ->
            mockMvc.post("/auth/request-password-reset") {
                param("email", account.emailAddress)

                with(SecurityMockMvcRequestPostProcessors.csrf())
            }.andExpect {
                status {
                    is3xxRedirection()
                }
            }

            val resetCode =
                accountRepository.findAccountByEmailAddressIgnoreCase(account.emailAddress)?.accountPasswordReset?.resetCode!!

            mockMvc.get("/auth/password-reset") {
                param("code", resetCode.toString())
            }.andExpect {
                status {
                    is2xxSuccessful()
                }
            }

            mockMvc.post("/auth/password-reset") {
                param("email", account.emailAddress)
                param("code", resetCode.toString())
                param("password", "NewPass213!")
                param("passwordConfirm", "NewPass213!")

                with(SecurityMockMvcRequestPostProcessors.csrf())
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

    @Test
    internal fun `GIVEN an existing user WHEN attempting to reset password without valid code THEN return failure`() {
        withTestAccount {
            mockMvc.get("/auth/password-reset") {
                with(SecurityMockMvcRequestPostProcessors.csrf())
            }.andExpect {
                status {
                    is4xxClientError()
                }
            }

            mockMvc.get("/auth/password-reset") {
                with(SecurityMockMvcRequestPostProcessors.csrf())
                param("code", "12345")
            }.andExpect {
                status {
                    is4xxClientError()
                }
            }
        }
    }

}
