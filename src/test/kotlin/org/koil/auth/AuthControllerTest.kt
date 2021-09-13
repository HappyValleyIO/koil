package org.koil.auth

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.koil.BaseIntegrationTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import kotlin.random.Random

class AuthControllerTest : BaseIntegrationTest() {

    @Nested
    inner class RegistrationTests {

        @Test
        internal fun `GIVEN no user with existing credentials WHEN registering THEN redirect to dashboard`() {
            mockMvc.post("/auth/register") {
                param("email", "${Math.random()}@example.com")
                param("handle", "${Random.nextInt(0, Integer.MAX_VALUE)}000000".substring(0, 8))
                param("password", "SomethingSecure1!")
                param("name", "Test User")

                with(csrf())
            }.andExpect {
                status {
                    is3xxRedirection()
                }
                header {
                    string("location", "/dashboard")
                }
            }
        }

        @Test
        internal fun `GIVEN a user with existing credentials WHEN registering same user THEN return bad request`() {
            withTestAccount { account ->
                mockMvc.post("/auth/register") {
                    param("email", account.emailAddress)
                    param("handle", account.handle)
                    param("password", "SomethingSecure1!")
                    param("name", "Test User")

                    with(csrf())
                }.andExpect {
                    status {
                        is4xxClientError()
                    }
                }
            }
        }
    }

    @Nested
    inner class PasswordResetTests {
        @Test
        internal fun `GIVEN an existing user WHEN resetting password THEN succeed`() {
            withTestAccount { account ->
                mockMvc.post("/auth/request-password-reset") {
                    param("email", account.emailAddress)

                    with(csrf())
                }.andExpect {
                    status {
                        is2xxSuccessful()
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

                    with(csrf())
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
                    with(csrf())
                }.andExpect {
                    status {
                        is4xxClientError()
                    }
                }

                mockMvc.get("/auth/password-reset") {
                    with(csrf())
                    param("code", "12345")
                }.andExpect {
                    status {
                        is4xxClientError()
                    }
                }
            }
        }
    }
}
